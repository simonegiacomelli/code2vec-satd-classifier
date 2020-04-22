package core

import core.Ignore.exception
import org.slf4j.LoggerFactory
import java.lang.Runnable
import kotlin.system.exitProcess

/* Simone 11/10/13 12.32 */

object Shutdown  {
    val log = LoggerFactory.getLogger(Shutdown::class.java)

    @Volatile
    var isShuttingDown = false
    val listeners = mutableListOf<()->Unit>()

    init {
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                notifyAllRemaining()
            }
        })
    }

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
    private fun exit(exitCode: Int) {
        if (isShuttingDown) {
            log.info("Stop already issued; exit code [{}] swallowed", exitCode)
            return
        }
        log.info("Shutdown.exit({})", exitCode)
        notifyAllRemaining()
        exitProcess(exitCode)
    }

    fun hook() {
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                log.info("Shutting down")
            }
        })
        Daemon().start("Shutdown safeguard watch", Runnable {
            while (true) {
                if (stopping()) {
                    Sleep.sleepStatic(30000)
                    log.warn("Still alive. Halting")
                    Runtime.getRuntime().halt(4)
                    return@Runnable
                }
                Sleep.sleepStatic(1000)
            }
        })
    }


}