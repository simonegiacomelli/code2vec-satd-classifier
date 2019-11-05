package satd.step2

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffEntry.ChangeType.*
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.lib.AbbreviatedObjectId
import org.eclipse.jgit.lib.AnyObjectId
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.treewalk.AbstractTreeIterator
import org.eclipse.jgit.util.io.DisabledOutputStream
import org.eclipse.jgit.treewalk.CanonicalTreeParser
import org.eclipse.jgit.treewalk.EmptyTreeIterator
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import satd.step1.Folders
import satd.utils.AntiSpin
import satd.utils.Rate
import satd.utils.printStats
import java.nio.charset.Charset


/**
 * Creates graph of satd evolution through commits
 */
class Main(val git: Git) {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            setupDatabase()
            val git = satd_gp1().apply { rebuild() }.git
//            val git = Git.open(Folders.repos.resolve("PhilJay_MPAndroidChart").toFile())
//            val git = Git.open(Folders.repos.resolve("square_retrofit").toFile())
//            val git = Git.open(Folders.repos.resolve("google_guava").toFile())
//            val git = Git.open(Folders.repos.resolve("elastic_elasticsearch").toFile())
            git.printStats()
//    val commits = Collector(git.repository).commits()
            val g = Main(git).apply { trackSatd() }
//            DotGraph(g.allSatds, git.repository.workTree.name).full()
        }
    }

    val repo = git.repository
    val repoName = repo.workTree.name
    val allSatds = mutableMapOf<ObjectId, SourceInfo>()

    val reader = git.repository.newObjectReader()
    val emptyTreeIterator = EmptyTreeIterator()

    val edgeRate = Rate(10)
    val sourceRate = Rate(10)
    val satdRate = Rate(10)
    val commitRate = Rate(10)
    val ratePrinter =
        AntiSpin { println("commit#:${commitRate.spinCount} edge#:${edgeRate.spinCount} source#:${sourceRate.spinCount} edge/sec:$edgeRate source/sec:$sourceRate satd/sec: $satdRate") }

    fun trackSatd() {


        for (child in git.log().all().call()) {
            commitRate.spin()
            if (child.parents.isNotEmpty())
                child.parents.forEach {
                    val parent = it
                    visitEdge(child.newTreeIterator(), parent.newTreeIterator(), child, parent)
                }
            else visitEdge(child.newTreeIterator(), emptyTreeIterator, child, ObjectId.zeroId())

            ratePrinter.spin()
        }
        ratePrinter.callback()

    }

    private fun RevCommit.newTreeIterator() = CanonicalTreeParser().apply { reset(reader, tree) }
    private fun AbbreviatedObjectId.source() = processedSatds(this.toObjectId())
    private fun DiffEntry.isJavaSource() = this.newPath.endsWith(".java") || this.oldPath.endsWith(".java")
    private fun ObjectId.content() = repo.open(this).bytes.toString(Charset.forName("UTF-8"))

    private fun visitEdge(
        childIterator: AbstractTreeIterator,
        parentIterator: AbstractTreeIterator,
        childCommit: ObjectId,
        parentCommit: ObjectId
    ) {
        edgeRate.spin()
        DiffFormatter(DisabledOutputStream.INSTANCE).use { formatter ->
            formatter.setRepository(git.repository)
            val entries = formatter.scan(parentIterator, childIterator)

            entries
                .filterNotNull()
                .filter { it.isJavaSource() }
                .forEach {
                    ratePrinter.spin()
                    when (it.changeType) {
                        MODIFY -> it.newId.source().link(it.oldId.source(), parentCommit, childCommit)
                        COPY, RENAME, ADD, DELETE -> {
                            /* should not matter to our satd tracking */
                        }
                        null -> TODO()
                    }

                }
        }
    }

    private fun processedSatds(objectId: ObjectId): SourceInfo {
        val objectSatd = allSatds.getOrPut(objectId) {
            sourceRate.spin()
            val content = objectId.content()
            val satdMethods = findMethodsWithSatd(content)

            if (satdMethods.isNotEmpty())
                satdRate.spin()

            SourceInfo(objectId, satdMethods.map { it.name to it }.toMap().toMutableMap())
        }
        return objectSatd
    }


    inner class SourceInfo(val objectId: ObjectId, val methods: MutableMap<String, Method>) {

        fun link(oldSource: SourceInfo, oldCommitId: AnyObjectId, newCommitId: AnyObjectId) {
            //we are interested in disappearing satd to the next state of the method
            //we are not interested in the previous state of a method with satd

            val oldSatd = oldSource.methods.values.filter { it.hasSatd }
            val oldNamesWithSatd = oldSatd.map { it.name }.toSet()
            val missingNewMethodsNames = oldNamesWithSatd.subtract(methods.keys)

            val missingNewMethods = findMethodsByName(objectId.content(), missingNewMethodsNames)
            methods.putAll(missingNewMethods.map { it.name to it })

            //now old and new contains matching methods instances
            oldSatd.forEach { old ->
                val new = methods.get(old.name)!!
                if (old.hasSatd && new.exists && !new.hasSatd) {
                    println("-".repeat(50))
                    println("from ${oldCommitId.abb} to ${newCommitId.abb} satd disappeared")
                    println("old------------------")
                    println("${old.method}")
                    println("new------------------")
                    println("${new.method}")
                    transaction {
                        DbSatds.insert {
                            it[this.repo] = repoName
                            it[this.commit] = "${newCommitId.name}"
                            it[this.satd] = "${old.method}"
                            it[this.fixed] = "${new.method}"
                        }
                    }
                }
            }
        }


    }
}


private val AnyObjectId.esc get() = "\"$this\""
private val AnyObjectId.abb get() = "${this.abbreviate(7).name()}"