package satd.step2

import org.eclipse.jgit.api.errors.JGitInternalException
import org.eclipse.jgit.api.errors.NoHeadException
import org.eclipse.jgit.errors.IncorrectObjectTypeException
import org.eclipse.jgit.errors.MissingObjectException
import org.eclipse.jgit.internal.JGitText
import org.eclipse.jgit.lib.AnyObjectId
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevWalk
import java.io.IOException
import java.text.MessageFormat

/**
 * Customized RevWalk that holds [CRevCommit] type
 */
class CRevWalk(val repo: Repository) : RevWalk(repo) {
    val commits = mutableMapOf<AnyObjectId, CRevCommit>()

    fun link(parent: RevCommit, child: RevCommit) {
        val p = parent as CRevCommit
        val c = child as CRevCommit
        p.childs.add(c)
    }

    override fun createCommit(id: AnyObjectId): RevCommit {
        val satdCommit = CRevCommit(id)
        commits.put(id, satdCommit)
//        println("crea $satdCommit")
        return satdCommit
    }

    var startSpecified = false

    fun all() {
        for (refi in repo.refDatabase.refs) {
            val ref = if (!refi.isPeeled) repo.refDatabase.peel(refi) else refi

            var objectId: ObjectId? = ref.getPeeledObjectId()
            if (objectId == null)
                objectId = ref.getObjectId()
            var commit: RevCommit? = null
            try {
                commit = parseCommit(objectId)
            } catch (e: MissingObjectException) {
                // ignore as traversal starting point:
                // - the ref points to an object that does not exist
                // - the ref points to an object that is not a commit (e.g. a
                // tree or a blob)
            } catch (e: IncorrectObjectTypeException) {
            }

            if (commit != null)
                add(commit)
        }

    }

    fun call(): Iterable<CRevCommit> {
        if (!startSpecified) {
            try {
                val headId = repo.resolve(Constants.HEAD)
                    ?: throw NoHeadException(
                        JGitText.get().noHEADExistsAndNoExplicitStartingRevisionWasSpecified
                    )
                add(headId)
            } catch (e: IOException) {
                // all exceptions thrown by add() shouldn't occur and represent
                // severe low-level exception which are therefore wrapped
                throw JGitInternalException(
                    JGitText.get().anExceptionOccurredWhileTryingToAddTheIdOfHEAD,
                    e
                )
            }

        }

        if (this.revFilter != null) {
            setRevFilter(this.revFilter)
        }
        return this as Iterable<CRevCommit>

    }

    @Throws(MissingObjectException::class, IncorrectObjectTypeException::class)
    fun add(start: AnyObjectId) {
        add(true, start)
    }

    @Throws(MissingObjectException::class, IncorrectObjectTypeException::class, JGitInternalException::class)
    private fun add(include: Boolean, start: AnyObjectId) {

        try {
            if (include) {
                markStart(lookupCommit(start))
                startSpecified = true
            } else
                markUninteresting(lookupCommit(start))

        } catch (e: MissingObjectException) {
            throw e
        } catch (e: IncorrectObjectTypeException) {
            throw e
        } catch (e: IOException) {
            throw JGitInternalException(
                MessageFormat.format(
                    JGitText.get().exceptionOccurredDuringAddingOfOptionToALogCommand, start
                ), e
            )
        }

    }

}