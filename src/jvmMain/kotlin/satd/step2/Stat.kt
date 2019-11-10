package satd.step2

import satd.utils.AntiSpin
import satd.utils.Rate

class Stat(repoName: String, commitCount: Int) {
    /**
     * every invocation will print stats
     */
    fun printForce() {
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
            println(
                "${repoName.padEnd(50)} commit#:${commitRate.spinCount}/$commitCount source#:${sourceRate.spinCount}  satd#:${satdRate.spinCount} " +
                        "satd/sec: $satdRate source/sec:$sourceRate "
            )
        }

}