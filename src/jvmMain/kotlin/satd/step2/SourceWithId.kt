package satd.step2

import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.lib.AnyObjectId
import org.eclipse.jgit.lib.ObjectId

/**
 * Contains all the satd of this git source file (git blob object)
 */
class SourceWithId(src: AnyObjectId, content: String) : ObjectId(src) {
    val satdList = Source(content).satdList

    val parents = mutableMapOf<SourceWithId, MutableList<Info>>()
//    val childs = mutableSetOf<SourceWithId>()
    val names = mutableSetOf<String>()
    val commits = mutableSetOf<AnyObjectId>()

    fun add(info: Info) {
        names.add(info.newPath)
        commits.add(info.newCommitId)
    }

    fun modify(
        info: Info,
        oldSatd: SourceWithId
    ) {
        val parentCommits = parents.getOrPut(oldSatd) { mutableListOf() }
        parentCommits.add(info)
//        oldSatd.childs.add(this)
        names.add(info.newPath)
        commits.add(info.newCommitId)
    }

    fun delete(info: Info) {
    }

    override fun toString(): String {
        return abbreviate(7).name()
    }
}


data class Info(
    val changeType: DiffEntry.ChangeType,
    val oldCommitId: AnyObjectId,
    val newCommitId: AnyObjectId,
    val newId: AnyObjectId,
    val oldId: AnyObjectId,
    val newPath: String,
    val oldPath: String
)