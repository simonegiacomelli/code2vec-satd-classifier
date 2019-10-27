package satd.step2

import org.eclipse.jgit.lib.AnyObjectId
import org.eclipse.jgit.revwalk.RevCommit

/**
 * Customized RevCommit that holds also reference to its child
 */
class CRevCommit(id: AnyObjectId) : RevCommit(id) {
    val childs = mutableListOf<CRevCommit>()
}