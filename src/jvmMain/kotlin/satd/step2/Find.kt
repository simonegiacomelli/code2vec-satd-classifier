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
import satd.utils.Repo
import satd.utils.printStats


/**
 * Find satd through commits
 */
class Find(val repo: Repo) {


    // some utility extension methods
    private fun RevCommit.newTreeIterator() = CanonicalTreeParser().apply { reset(reader, tree) }

    private fun AbbreviatedObjectId.source() = blobSatd.processedSatds(this.toObjectId())
    private fun DiffEntry.isJavaSource() = this.newPath.endsWith(".java") || this.oldPath.endsWith(".java")

    private val AnyObjectId.esc get() = "\"$this\""
    private val AnyObjectId.abb get() = "${this.abbreviate(7).name()}"

    val git: Git by lazy { repo.newGit() }
    val stat by lazy { Stat(repo, commitCount = git.log().all().call().count()) }
    val blobSatd by lazy { BlobSatd(git.repository, stat) }
    val reader by lazy { git.repository.newObjectReader() }

    val emptyTreeIterator = EmptyTreeIterator()

    fun trackSatd() {
        try {
            git.printStats()
            trackSatdInternal()
            stat.done()
            DbRepos.done(repo.urlstr)
        } catch (ex: Throwable) {
            DbRepos.failed(repo.urlstr, ex)
            Exceptions(ex, git.repository.workTree.name).handle()
            throw ex
        }
    }

    fun trackSatdInternal() {


        for (child in git.log().all().call()) {
            stat.commitRate.spin()
            if (child.parents.isNotEmpty())
                child.parents.forEach {
                    val parent = it
                    visitEdge(child.newTreeIterator(), parent.newTreeIterator(), child, parent)
                }
            else visitEdge(child.newTreeIterator(), emptyTreeIterator, child, ObjectId.zeroId())

            stat.printSpin()
        }

    }

    private fun visitEdge(
        childIterator: AbstractTreeIterator,
        parentIterator: AbstractTreeIterator,
        childCommit: RevCommit,
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


