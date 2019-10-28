package satd.step2

import org.eclipse.jgit.lib.AnyObjectId
import org.eclipse.jgit.lib.ObjectId

/**
 * Contains all the satd of this git source file (git blob object)
 */
class Blob(src: AnyObjectId, val content: String) : ObjectId(src) {
    fun init(): Blob {
        return this
    }
}