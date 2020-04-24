package satd.step2

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.treewalk.TreeWalk


fun failIfToReject(git: Git) {

    val bad = setOf("Android.mk", "CleanSpec.mk")
    if (containsAll(git, bad))
        throw RejectedRepositoryException("Android OS repository is not accepted")

}

private fun containsAll(git: Git, bad: Set<String>): Boolean {
    val repository = git.repository
    val head = repository.findRef("HEAD")

    // a RevWalk allows to walk over commits based on some filtering that is defined
    RevWalk(repository).use { walk ->
        val commit = walk.parseCommit(head.objectId)
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
