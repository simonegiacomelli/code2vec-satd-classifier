package satd.step2

import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.treewalk.TreeWalk
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter
import sample.jgit.customized.gp1

/**
 * Tracker of SATD across the repository history
 */
class Tracker(val repo: Repository) {

    companion object{
        @JvmStatic
        fun main(args: Array<String>) {
            val gp = gp1()
            Tracker(gp.git.repository).walk()
        }
    }
    fun walk() {

        val walk = CRevWalk(repo)
        walk.all()
        for (commit in walk.call()) {
            commit.parents.forEach { parent -> walk.link(parent, commit) }
            findSatd(commit)
        }

    }

    private fun findSatd(commit: CRevCommit) {
        println("$commit FILES:")
        val treeWalk = TreeWalk(repo)
        treeWalk.addTree(commit.tree)
        treeWalk.isRecursive = true
        treeWalk.filter = PathSuffixFilter.create(".java")
        while (treeWalk.next())
            println("  ${treeWalk.pathString}")
    }


}