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
import satd.utils.printStats


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

    private fun AbbreviatedObjectId.source() = blobSatd.processedSatds(this.toObjectId())
    private fun DiffEntry.isJavaSource() = this.newPath.endsWith(".java") || this.oldPath.endsWith(".java")

    private val AnyObjectId.esc get() = "\"$this\""
    private val AnyObjectId.abb get() = "${this.abbreviate(7).name()}"

    val repo = git.repository
    val repoName = repo.workTree.name
    val stat = Stat(repoName, commitCount = git.log().all().call().count())
    val blobSatd = BlobSatd(repo, stat)

    val reader = git.repository.newObjectReader()

    val emptyTreeIterator = EmptyTreeIterator()

    fun trackSatd() {
        git.printStats()
        blobSatd.cache.load()

        for (child in git.log().all().call()) {
            stat.commitRate.spin()
            if (child.parents.isNotEmpty())
                child.parents.forEach {
                    val parent = it
                    visitEdge(child.newTreeIterator(), parent.newTreeIterator(), child, parent)
                }
            else visitEdge(child.newTreeIterator(), emptyTreeIterator, child, ObjectId.zeroId())

            stat.printSpin()
            blobSatd.cache.storeSpin()
        }
        stat.printForce()
        blobSatd.cache.store()
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
                    stat.ratePrinter.spin()
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

