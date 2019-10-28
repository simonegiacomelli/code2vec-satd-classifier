package satd.step2

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.treewalk.TreeWalk
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter
import satd.step1.Folders
import satd.utils.AntiSpin
import satd.utils.Rate
import java.nio.charset.Charset

/**
 * Tracker of SATD across the repository history
 */
class Tracker(val repo: Repository) {
    val blobsSatdSet = mutableSetOf<Blob>()

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
//            val gp = gp1()
//            gp.rebuild()
//            val git = gp.git
//            val git = Git.open(Folders.repos.resolve("square_retrofit").toFile())
//            val git = Git.open(Folders.repos.resolve("google_guava").toFile())
            val git = Git.open(Folders.repos.resolve("elastic_elasticsearch").toFile())
            Tracker(git.repository).walk()
        }
    }

    val commitRate = Rate(10)
    val blobRate = Rate(10)
    val ratePrinter = AntiSpin { "commits/sec:${commitRate.rate()} files/sec:${blobRate.rate()}" }

    fun walk() {
        commitRate.reset()
        blobRate.reset()

        val walk = CRevWalk(repo)
        walk.all()
        var counter = 0
        for (commit in walk.call()) {
            counter++
            commitRate.spin()
            commit.parents.forEach { parent -> walk.link(parent, commit) }
            findSatd(commit)
        }

    }

    private fun findSatd(commit: CRevCommit) {
        val treeWalk = TreeWalk(repo)
        treeWalk.addTree(commit.tree)
        treeWalk.filter = PathSuffixFilter.create(".java")
        treeWalk.isRecursive = true
        while (treeWalk.next()) {
            val objectId = treeWalk.getObjectId(0)!!
            if (!blobsSatdSet.contains(objectId)) {
                val content = repo.open(objectId).bytes.toString(Charset.forName("UTF-8"))
                val blobsSatd = Blob(objectId, content).init()
                blobsSatdSet.add(blobsSatd)
                blobRate.spin()
            } else
            ;
            ratePrinter.spin()
        }
    }


}
