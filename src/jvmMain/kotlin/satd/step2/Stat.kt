package satd.step2

import kotlinx.atomicfu.AtomicInt
import satd.utils.AntiSpin
import satd.utils.Rate
import satd.utils.Repo
import satd.utils.logln
import java.util.concurrent.atomic.AtomicInteger

class Stat(val repo: Repo, commitCount: Int) {

    /**
     * every invocation will print stats
     */
    fun done() {
        ratePrinter.callback()
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

    val ratePrinter =
        AntiSpin(10000) {
            logln(
                "${repo.urlstr} commit#:${commitRate.spinCount}/$commitCount source#:${sourceRate.spinCount}  satd#:${satdRate.spinCount} " +
                        "satd/sec: $satdRate source/sec:$sourceRate"
            )
        }

}