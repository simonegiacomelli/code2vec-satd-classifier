package satd.step2

import java.util.*

/**
 * Creates graph of satd evolution through commits
 */
class Grapher(val commits: MutableCollection<CRevCommit>) {

    fun trackSatd() {

        val sinks = commits
            .filter {
                it.visited = false
                it.parents.isEmpty()
            }

        sinks.forEach { parent ->
            visitCommit(parent)
        }
    }

    private fun visitCommit(root: CRevCommit) {
        val queue = LinkedList<CRevCommit>()
        queue.addFirst(root)

        while (queue.isNotEmpty()) {
            val commit = queue.pop()
            if (commit.visited)
                continue
            commit.visited = true

            commit.childs.forEach { child ->
                queue.addFirst(child)
                visitEdge(commit, child)
            }
        }
    }

    private fun visitEdge(parent: CRevCommit, child: CRevCommit) {
        println("visiting $parent ----> $child")
    }

}

