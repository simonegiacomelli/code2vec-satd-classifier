package satd.step2

import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.lib.AnyObjectId
import org.eclipse.jgit.lib.ObjectId

/**
 * Contains all the satd of this git source file (git blob object)
 */
class SourceWithId(src: AnyObjectId, content: String) : ObjectId(src) {
    val satdList = Source(content).satdList

    val parents = mutableListOf<SourceWithId>()


    fun add(diff: DiffEntry) {
    }

    fun linkOld(diff: DiffEntry, old: SourceWithId) {
        parents.add(old)
    }

    fun delete(diff: DiffEntry) {
    }
}