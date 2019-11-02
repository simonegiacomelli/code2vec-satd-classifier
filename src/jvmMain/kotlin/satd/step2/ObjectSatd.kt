package satd.step2

import org.eclipse.jgit.lib.AnyObjectId
import org.eclipse.jgit.lib.ObjectId

/**
 * Contains all the satd of this git source file (git blob object)
 */
class ObjectSatd(src: AnyObjectId, content: String) : ObjectId(src) {
    val list = Source(content).satdList
}