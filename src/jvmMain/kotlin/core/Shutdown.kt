package core

import core.Ignore.exception
import org.slf4j.LoggerFactory
import java.lang.Runnable
import java.util.*

/* Simone 11/10/13 12.32 */   class Shutdown protected constructor() {
    @Volatile
    var isShuttingDown = false
    var listeners = ArrayList<()->Unit>()
    fun stopping(): Boolean {
        return isShuttingDown
    }

    fun addApplicationShutdownHook(runnable: ()->Unit) {
        listeners.add(runnable)
    }

    private fun notifyAllRemaining() {
        isShuttingDown = true
        for (runnable in listeners.toTypedArray()) {
            exception(runnable)
            listeners.remove(runnable)
        }
    }

    @Synchronized
    private fun exitInternal(exitCode: Int) {
        if (isShuttingDown) {
            log.info("Stop already issued; exit code [{}] swallowed", exitCode)
            return
        }
        log.info("Shutdown.exit({})", exitCode)
        def()!!.notifyAllRemaining()
        System.exit(exitCode)
    }

    fun hook() {
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                log.info("Shutting down")
            }
        })
        Daemon().start("Shutdown safeguard watch", Runnable {
            while (true) {
                if (def()!!.stopping()) {
                    Sleep.sleepStatic(30000)
                    log.warn("Still alive. Halting")
                    Runtime.getRuntime().halt(4)
                    return@Runnable
                }
                Sleep.sleepStatic(1000)
            }
        })
    }

    companion object {
        val log = LoggerFactory.getLogger(Shutdown::class.java)
        var def: Shutdown? = null

        @Synchronized
        fun def(): Shutdown {
            if (def == null) def = Shutdown()
            return def!!
        }

        fun exit(exitCode: Int) {
            def()!!.exitInternal(exitCode)
        }
    }

    init {
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                notifyAllRemaining()
            }
        })
    }
}