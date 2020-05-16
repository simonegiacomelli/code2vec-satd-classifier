package satd.utils

import sun.misc.Signal
import sun.misc.SignalHandler
import java.lang.management.ManagementFactory
import java.lang.management.ThreadMXBean


val pid by lazy { "too-slow" /*ManagementFactory.getRuntimeMXBean().name.split("@")[0] */ }

fun main(args: Array<String>) {
    Diagnosis.installALRM()
    println("$pid")
    while (true)
        Thread.sleep(10000)
}

class Diagnosis : SignalHandler {
    private var oldHandler: SignalHandler? = null

    override fun handle(sig: Signal) {

        println("Diagnostic Signal handler called for signal $sig")
        val threadMxBean: ThreadMXBean = ManagementFactory.getThreadMXBean()

        for (ti in threadMxBean.dumpAllThreads(true, true)) {
            print(ti.toString())
        }
        if (oldHandler !== SignalHandler.SIG_DFL && oldHandler !== SignalHandler.SIG_IGN)
            oldHandler!!.handle(sig)
    }

    companion object {

        fun installALRM() = install("ALRM")

        fun install(signalName: String?): Diagnosis {
            val diagSignal = Signal(signalName)
            val diagHandler = Diagnosis()
            diagHandler.oldHandler = Signal.handle(diagSignal, diagHandler)
            return diagHandler
        }
    }
}