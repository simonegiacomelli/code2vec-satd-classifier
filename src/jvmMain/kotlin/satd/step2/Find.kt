package satd.step2

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffEntry.ChangeType.*
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.lib.AnyObjectId
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.treewalk.AbstractTreeIterator
import org.eclipse.jgit.util.io.DisabledOutputStream
import org.eclipse.jgit.treewalk.CanonicalTreeParser
import org.eclipse.jgit.treewalk.EmptyTreeIterator
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter
import satd.utils.Repo
import satd.utils.logln


/**
 * Find satd through commits
 */
class Find(val repo: Repo) {


    // some utility extension methods
    private fun RevCommit.newTreeIterator() =
        CanonicalTreeParser().apply { reset(/*reader*/git.repository.newObjectReader(), tree) }

    private fun DiffEntry.isJavaSource() = this.newPath.endsWith(".java") && this.oldPath.endsWith(".java")

    private val AnyObjectId.esc get() = "\"$this\""
    private val AnyObjectId.abb get() = "${this.abbreviate(7).name()}"

    val git: Git by lazy { repo.newGit() }
    val stat by lazy { Stat(repo, commitCount = git.log().all().call().count()) }
    val blobSatd by lazy { BlobSatd(git.repository, stat) }
    val reader by lazy { git.repository.newObjectReader() }

    val emptyTreeIterator = EmptyTreeIterator()

    fun trackSatd() {
        try {
            logln("${repo.urlstr} SATD Find.trackSatd()") // ${git.stats()}
            failIfToReject(git)
            trackSatdInternal()
            stat.done()
            blobSatd.repoIsCompleted()
            DbRepos.done(repo.urlstr)
        } catch (ex: Throwable) {
            DbRepos.failed(repo.urlstr, ex, "Find.trackSatd()")
            Exceptions(ex, git.repository.workTree.name).handle()
        } finally {
            try {
                git.close()
            } catch (ex: Throwable) {
            }
        }
    }

    fun trackSatdInternal() {

        git.log().all().call().toList().stream().parallel().forEach { child ->
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
            formatter.pathFilter = PathSuffixFilter.create(".java")
            val entries = formatter.scan(parentIterator, childIterator)

            entries
                .filterNotNull()
                .filter { it.isJavaSource() }
                .forEach {
                    stat.ratePrinter.spin()
                    when (it.changeType) {
                        MODIFY ->
                            blobSatd.processedSatds(it.newId.toObjectId())
                                .link(blobSatd.processedSatds(it.oldId.toObjectId()), parentCommit, childCommit)
                        COPY, RENAME, ADD, DELETE -> {
                            /* should not matter to our satd tracking */
                        }
                        null -> TODO()
                    }

                }
        }
    }

}


