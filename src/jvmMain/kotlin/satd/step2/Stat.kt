package satd.step2

import kotlinx.atomicfu.AtomicInt
import satd.utils.AntiSpin
import satd.utils.Rate
import satd.utils.Repo
import satd.utils.logln
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.roundToInt
import kotlin.math.roundToLong

class Stat(val repo: Repo, commitCount: Int) {
    val start = System.currentTimeMillis()

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
                "${repo.urlstr} commit#:${commitRate.spinCount}/$commitCount commit/sec:$commitRate source#:${sourceRate.spinCount}  satd#:${satdRate.spinCount} " +
                        "satd/sec: $satdRate source/sec:$sourceRate ${age()}"
            )
        }

    private fun age(): String {
        val delta = System.currentTimeMillis() - start
        val mins = (delta.toDouble() / 100 / 60).roundToLong().toDouble() / 10
        return "age mins:$mins"
    }
}