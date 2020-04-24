package satd.step2

import kotlinx.coroutines.yield
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.ObjectChecker.tree
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.treewalk.TreeWalk


fun failIfToReject(git: Git) {
//    git.addFile("Android.mk", "x")
//    git.addFile("CleanSpec.mk", "x")
    val bad = setOf("Android.mk","CleanSpec.mk")
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
                    yield( treeWalk.pathString)
            }.asIterable()

            if(bad.subtract(actual).isEmpty())
                throw RejectedRepositoryException("Android OS repository is not accepted")
        }
    }

}
class RejectedRepositoryException(message: String?) : Exception(message)
