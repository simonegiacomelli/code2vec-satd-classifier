package pgsql

import core.AppProperties
import core.Shutdown
import org.slf4j.LoggerFactory
import pgsql.ctl.*
import java.io.File

/* Simone 08/07/2014 17:49 */
class PgSqlStarter(private val pgSqlCtl: IPgSqlCtl) {
    @JvmField
    val log = LoggerFactory.getLogger(javaClass)
    fun start() {
        if (!pgSqlCtl.dbExist()) {
            pgSqlCtl.initDb()
            pgSqlCtl.start()
        } else {
            val status = pgSqlCtl.status()
            if (status == CtlStatus.NO_SERVER_RUNNING) pgSqlCtl.start() else if (status == CtlStatus.RUNNING) log.warn("PgSql already running")
        }
    }

    fun hookShutdown() {
        Shutdown.def().addApplicationShutdownHook {
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

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val appProp = File("file.conf")
            val pgsqlctl = PgSqlCtl(PgSqlConfigFix(), AppProperties(appProp))
            val pgSqlStarter = PgSqlStarter(pgsqlctl)
            //pgsqlctl.stop();
            pgSqlStarter.start()
            pgSqlStarter.hookShutdown()
        }
    }

}