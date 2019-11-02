package satd.step2

import org.eclipse.jgit.lib.AnyObjectId
import org.eclipse.jgit.revwalk.RevCommit

/**
 * Customized RevCommit that holds also reference to its child
 * and
 */
class CRevCommit(id: AnyObjectId) : RevCommit(id) {
    var visited = false
    val childs = mutableListOf<CRevCommit>()
    /**
     * map between Blob<-->FilenameList
     */
    val blobWithSatd = mutableMapOf<Satds, MutableList<String>>()

    fun addReverseEdges() {
        parents.forEach { (it as CRevCommit).childs.add(this) }
    }

    fun addSatd(satds: Satds, filename: String) {
        if (satds.list.isNotEmpty()) {
            val filenameList = blobWithSatd.getOrPut(satds) { mutableListOf() }
            filenameList.add(filename)
        }
    }
}