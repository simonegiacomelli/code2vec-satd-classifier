package satd.step2

import kotlinx.atomicfu.AtomicInt
import satd.utils.AntiSpin
import satd.utils.Rate
import satd.utils.Repo
import satd.utils.logln
import java.util.concurrent.atomic.AtomicInteger

class Stat(val repo: Repo, commitCount: Int) {
    companion object {
        var totRepo: Int = 0
        val repoDone = AtomicInteger(0)
    }

    /**
     * every invocation will print stats
     */
    fun done() {
        ratePrinter.callback()
        repoDone.incrementAndGet()
    }

    /**
    every invocation will NOT print stats. it will honor the anti-spin time window
     */
    fun printSpin() {
        ratePrinter.spin()
    }

    val sourceRate = Rate(10)
    val satdRate = Rate(10)
    val commitRate = Rate(10)

    val rt = Runtime.getRuntime()
    val mb = 1024 * 1024

    val ratePrinter =
        AntiSpin(10000) {
            logln(
                "${repo.urlstr} commit#:${commitRate.spinCount}/$commitCount source#:${sourceRate.spinCount}  satd#:${satdRate.spinCount} " +
                        "satd/sec: $satdRate source/sec:$sourceRate ${mem()} totRepos:${repoDone.get()}/$totRepo"
            )
        }

    private fun mem(): String {
        val used = (rt.totalMemory() - rt.freeMemory()) / mb
        val m = rt.maxMemory() / mb
        return "mem:$used/$m"
    }

}