package pgsql

import core.Shutdown
import org.slf4j.LoggerFactory
import pgsql.ctl.*

/* Simone 08/07/2014 17:49 */
class PgSqlStarter(val pgSqlCtl: PgSqlCtl = PgSqlCtl()) {

    companion object {
        val def by lazy { PgSqlStarter() }

        @JvmStatic
        fun main(args: Array<String>) {
            //pgsqlctl.stop();
            def.start()
            println("Start done")
            while (true) Thread.sleep(10000)
        }
    }

    val log = LoggerFactory.getLogger(javaClass)
    fun start(hookShutdown: Boolean = true) {
        val startedByUs = if (!pgSqlCtl.dbExist()) {
            pgSqlCtl.initDb()
            pgSqlCtl.start()
            true
        } else {
            val status = pgSqlCtl.status()
            val notRunning = status == CtlStatus.NO_SERVER_RUNNING
            if (notRunning)
                pgSqlCtl.start()
            else
                if (status == CtlStatus.RUNNING)
                    log.warn("PgSql already running")
            notRunning
        }
        if (startedByUs && hookShutdown)
            hookShutdown()
    }

    private fun hookShutdown() {
        Shutdown.addApplicationShutdownHook {
            log.info("Stopping PgSql")
            if (smartShutdownFailed()) {
                if (pgSqlCtl.stopFast() == StopStatus.STOP_FAILED) log.error("Unable to stop PgSql")
            }
        }
    }

    private fun smartShutdownFailed(): Boolean {
        val result = pgSqlCtl.stop() == StopStatus.STOP_FAILED
        if (result) log.warn("Smart shutdown failed. Issuing fast shutdown")
        return result
    }

}