package satd.step2

import satd.utils.Rate
import satd.utils.logln
import java.util.concurrent.atomic.AtomicInteger

class RepoRate {
    fun alreadyDone(size: Int) {
        repoDone.getAndSet(size)
        logStat()
    }

    var totRepo: Int = 0
    val repoDone = AtomicInteger(0)


    private val rate = Rate(60)

    @Synchronized
    fun spin() {
        rate.spin()
        repoDone.incrementAndGet()
    }

    @Synchronized
    fun rate(): Double = rate.rate()

    @Synchronized
    private fun logStat() {
        logln("totRepos:${repoDone.get()}/$totRepo repo/sec ${rate()} $mem")
    }

    fun startStatAsync() {
        Thread {
            while (true) {
                Thread.sleep(10000)
                logStat()
            }
        }.apply {
            isDaemon = true
            name = "stat"
        }.start()
    }

    val rt = Runtime.getRuntime()
    val mb = 1024 * 1024

    private val mem: String
        get() {
            val used = (rt.totalMemory() - rt.freeMemory()) / mb
            val m = rt.maxMemory() / mb
            return "mem:$used/$m"
        }

}

val repoRate = RepoRate()

