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
    val blobWithSatd = mutableMapOf<Blob, MutableList<String>>()

    fun addReverseEdges() {
        parents.forEach { (it as CRevCommit).childs.add(this) }
    }

    fun addSatd(blob: Blob, filename: String) {
        val filenameList = blobWithSatd.getOrPut(blob) { mutableListOf() }
        filenameList.add(filename)
    }
}