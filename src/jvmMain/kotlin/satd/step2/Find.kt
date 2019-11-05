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
import satd.utils.stats
import java.nio.charset.Charset


/**
 * Find satd through commits
 */
class Find(val git: Git) {

    companion object {
        init {
            setupDatabase()
        }
    }

    // some utility extension methods
    private fun RevCommit.newTreeIterator() = CanonicalTreeParser().apply { reset(reader, tree) }

    private fun AbbreviatedObjectId.source() = processedSatds(this.toObjectId())
    private fun DiffEntry.isJavaSource() = this.newPath.endsWith(".java") || this.oldPath.endsWith(".java")
    private fun ObjectId.content() = repo.open(this).bytes.toString(Charset.forName("UTF-8"))
    private val AnyObjectId.esc get() = "\"$this\""
    private val AnyObjectId.abb get() = "${this.abbreviate(7).name()}"

    val repo = git.repository
    val repoName = repo.workTree.name
    val commitCount by lazy { git.log().all().call().count() }
    val allSatds = mutableMapOf<ObjectId, SourceInfo>()

    val reader = git.repository.newObjectReader()
    val emptyTreeIterator = EmptyTreeIterator()

    val sourceRate = Rate(10)
    val satdRate = Rate(10)
    val commitRate = Rate(10)
    val ratePrinter =
        AntiSpin(10000) {
            println(
                "${repoName.padEnd(50)} commit#:${commitRate.spinCount}/$commitCount source#:${sourceRate.spinCount}  satd#:${satdRate.spinCount} " +
                        "satd/sec: $satdRate source/sec:$sourceRate "
            )
        }

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

    private fun visitEdge(
        childIterator: AbstractTreeIterator,
        parentIterator: AbstractTreeIterator,
        childCommit: ObjectId,
        parentCommit: ObjectId
    ) {
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
                    transaction {
                        DbSatds.insert {
                            it[this.repo] = repoName
                            it[this.commit] = "${newCommitId.name}"
                            it[this.satd] = "${old.method}"
                            it[this.fixed] = "${new.method}"
                        }
                    }
                    satdRate.spin()
                }
            }
        }


    }
}

class FindMain {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val git = satd_gp1().apply { rebuild() }.git
//            val git = Git.open(Folders.repos.resolve("PhilJay_MPAndroidChart").toFile())
//            val git = Git.open(Folders.repos.resolve("square_retrofit").toFile())
//            val git = Git.open(Folders.repos.resolve("google_guava").toFile())
//            val git = Git.open(Folders.repos.resolve("elastic_elasticsearch").toFile())
            git.printStats()
//    val commits = Collector(git.repository).commits()
            val g = Find(git).apply { trackSatd() }
//            DotGraph(g.allSatds, git.repository.workTree.name).full()
        }
    }
}

