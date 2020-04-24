package satd.step2

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.treewalk.TreeWalk
import java.io.File

fun main() {
    val git = Git.init().setDirectory(File("data/repos/ckisgen/RoyalGinger-frameworks-base")).call()!!
    failIfToReject(git)
}

fun failIfToReject(git: Git) {

    val bad = setOf("Android.mk", "CleanSpec.mk")
    if (containsAll(git, bad))
        throw RejectedRepositoryException("Android OS repository is not accepted")

}

private fun containsAll(git: Git, bad: Set<String>): Boolean {
    git.log().all().call().toList().take(10).forEach { child ->
        child!!.apply {
            if (containsAllCommit(git.repository, this, setOf("Android.mk", "CleanSpec.mk")))
                return true
        }

    }
    return false
}

private fun containsAllCommit(
    repository: Repository?,
    objectId: ObjectId?,
    bad: Set<String>
): Boolean {
    RevWalk(repository).use { walk ->
        val commit = walk.parseCommit(objectId)
        val tree = commit.tree


        TreeWalk(repository).use { treeWalk ->
            treeWalk.addTree(tree)
            // not walk the tree recursively so we only get the elements in the top-level directory
            treeWalk.isRecursive = false
            val actual = sequence {
                while (treeWalk.next())
                    yield(treeWalk.pathString)
            }.asIterable()

            return bad.subtract(actual).isEmpty()

        }
    }
}

class RejectedRepositoryException(message: String?) : Exception(message)
