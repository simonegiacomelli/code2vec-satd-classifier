package satd.step2

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffEntry.ChangeType.*
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.lib.AbbreviatedObjectId
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.treewalk.AbstractTreeIterator
import org.eclipse.jgit.util.io.DisabledOutputStream
import org.eclipse.jgit.treewalk.CanonicalTreeParser
import org.eclipse.jgit.treewalk.EmptyTreeIterator
import satd.utils.AntiSpin
import satd.utils.Rate
import satd.utils.printStats
import java.nio.charset.Charset


/**
 * Creates graph of satd evolution through commits
 */
class Grapher3(val git: Git) {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val git = satd_gp1().apply { rebuild() }.git
//            val git = Git.open(Folders.repos.resolve("square_retrofit").toFile())
//            val git = Git.open(Folders.repos.resolve("google_guava").toFile())
//    val git = Git.open(Folders.repos.resolve("elastic_elasticsearch").toFile())
            git.printStats()
//    val commits = Collector(git.repository).commits()
            Grapher3(git).trackSatd()
        }
    }

    val repo = git.repository
    val allSatds = mutableMapOf<ObjectId, SourceWithId>()

    val reader = git.repository.newObjectReader()
    val emptyTreeIterator = EmptyTreeIterator()

    val edgeRate = Rate(10)
    val sourceRate = Rate(10)
    val satdRate = Rate(10)
    val commitRate = Rate(10)
    val ratePrinter =
        AntiSpin { println("commit#:${commitRate.spinCount} edge#:${edgeRate.spinCount} source#:${sourceRate.spinCount} edge/sec:$edgeRate source/sec:$sourceRate satd/sec: $satdRate") }
    val satdGraph = SatdGraph()

    fun trackSatd() {


        for (child in git.log().all().call()) {
            commitRate.spin()
            if (child.parents.isNotEmpty())
                child.parents.forEach {
                    val parent = it
                    visitEdge(child.newTreeIterator(), parent.newTreeIterator())
                }
            else visitEdge(child.newTreeIterator(), emptyTreeIterator)

            ratePrinter.spin()
        }
        ratePrinter.callback()
    }

    private fun RevCommit.newTreeIterator() = CanonicalTreeParser().apply { reset(reader, tree) }
    private fun AbbreviatedObjectId.satds() = processedSatds(this.toObjectId())
    private fun DiffEntry.isJavaSource() = this.newPath.endsWith(".java") || this.oldPath.endsWith(".java")

    private fun visitEdge(
        childIterator: AbstractTreeIterator,
        parentIterator: AbstractTreeIterator
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
                        ADD -> it.newId.satds().add(it)
                        MODIFY -> it.newId.satds().linkOld(it, it.oldId.satds())
                        DELETE -> it.oldId.satds().delete(it)
                        COPY, RENAME -> {
                            /*should not matter to our satd tracking*/
                        }
                        null -> TODO()
                    }

                }
        }
    }

    private fun processedSatds(objectId: ObjectId): SourceWithId {
        val objectSatd = allSatds.getOrPut(objectId) {
            sourceRate.spin()
            val content = repo.open(objectId).bytes.toString(Charset.forName("UTF-8"))
            val objectSatd = SourceWithId(objectId, content)
            if (objectSatd.satdList.isNotEmpty())
                satdRate.spin()
            objectSatd
        }
        return objectSatd
    }

}


class SatdGraph