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
    val blobWithSatd = mutableMapOf<ObjectSatd, MutableList<String>>()

    fun addReverseEdges() {
        parents.forEach { (it as CRevCommit).childs.add(this) }
    }

    fun addSatd(objectSatd: ObjectSatd, filename: String) {
        if (objectSatd.list.isNotEmpty()) {
            val filenameList = blobWithSatd.getOrPut(objectSatd) { mutableListOf() }
            filenameList.add(filename)
        }
    }
}