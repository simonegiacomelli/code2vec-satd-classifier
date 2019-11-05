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
    val blobWithSatd = mutableMapOf<SourceWithId, MutableList<String>>()

    fun addReverseEdges() {
        parents.forEach { (it as CRevCommit).childs.add(this) }
    }

    fun addSatd(sourceWithId: SourceWithId, filename: String) {
        if (sourceWithId.satdMethods.isNotEmpty()) {
            val filenameList = blobWithSatd.getOrPut(sourceWithId) { mutableListOf() }
            filenameList.add(filename)
        }
    }
}