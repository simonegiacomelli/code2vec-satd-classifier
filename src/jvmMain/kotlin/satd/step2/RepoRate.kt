package satd.step2

import satd.utils.Rate
import satd.utils.logln
import java.util.concurrent.atomic.AtomicInteger

class RepoRate {
    fun alreadyDone(size: Int) {
        alreadyDone = size
        logStat()
    }

    var totRepo: Int = 0
    private var alreadyDone = 0
    private val repoDone = AtomicInteger(0)


    private val rate = Rate(60)

    @Synchronized
    fun spin() {
        rate.spin()
        repoDone.incrementAndGet()
    }

    @Synchronized
    fun rateStr() = rate.toString()

    @Synchronized
    fun logStat() {
        logln("totRepos:${repoDone.get() + alreadyDone}/$totRepo this run:${repoDone.get()} repo/sec ${rateStr()} $mem")
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

