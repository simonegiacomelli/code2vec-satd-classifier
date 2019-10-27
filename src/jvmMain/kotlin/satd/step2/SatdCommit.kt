package satd.step2

import org.eclipse.jgit.lib.AnyObjectId
import org.eclipse.jgit.revwalk.RevCommit

class SatdCommit(id: AnyObjectId) : RevCommit(id) {
    val childs = mutableListOf<SatdCommit>()
}