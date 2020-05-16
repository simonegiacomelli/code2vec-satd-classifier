package satd.step2

import satd.utils.AntiSpin
import satd.utils.Rate
import satd.utils.Repo
import satd.utils.logln
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
            val commitn = p("${commitRate.spinCount}/$commitCount", 13)
            logln(
                "${p(age(), 10)} commit#:$commitn " +
                        "c/sec:${p(commitRate, 5)} src-done#:${p(sourceRate.spinCount, 6)} " +
                        "satd#:${p(satdRate.spinCount, 3)} " +
                        //"satd/sec:$satdRate " +
                        "src/sec:${p(sourceRate, 5)} ${repo.urlstr} "
            )
        }

    private fun p(inp: Any, l: Int): String = "$inp".padEnd(l)
    private fun age(): String {
        val delta = System.currentTimeMillis() - start

        val mins = (delta.toDouble() / 100 / 60).roundToLong().toDouble() / 10
        return "age:${mins}m"
    }
}