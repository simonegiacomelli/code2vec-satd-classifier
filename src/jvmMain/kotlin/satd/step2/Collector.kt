package satd.step2

import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.treewalk.TreeWalk
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter
import satd.utils.AntiSpin
import satd.utils.Rate

/**
 * Collect SATD across the repository history
 */
class Collector(val repo: Repository) {
    val blobs = mutableMapOf<ObjectId, Satds>()

    val commitRate = Rate(10)
    val blobRate = Rate(10)
    val satdRate = Rate(10)
    val ratePrinter =
        AntiSpin { println("commits/sec:$commitRate blob/sec:$blobRate satd/sec: $satdRate") }


    fun commits(): MutableCollection<CRevCommit> {
        commitRate.reset()
        blobRate.reset()
        val walk = CRevWalk(repo)
        walk.all()

        for (commit in walk.call())
            findSatd(commit)

        ratePrinter.callback()
        return walk.commits.values
    }

    private fun findSatd(commit: CRevCommit) {
        commitRate.spin()
        commit.addReverseEdges()
        val treeWalk = TreeWalk(repo)
        treeWalk.addTree(commit.tree)
        treeWalk.filter = PathSuffixFilter.create(".java")
        treeWalk.isRecursive = true
        while (treeWalk.next()) {
            val objectId = treeWalk.getObjectId(0)!!

            val blob = blobs.getOrPut(objectId) {
                blobRate.spin()
                //val content = repo.open(objectId).bytes.toString(Charset.forName("UTF-8"))
                val blob = Satds(objectId, "")
                if (blob.list.isNotEmpty())
                    satdRate.spin()
                blob
            }

            if (blob.list.isNotEmpty())
                commit.addSatd(blob, treeWalk.nameString)

            ratePrinter.spin()
        }
    }

}
