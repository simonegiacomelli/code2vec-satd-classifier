package pgsql;

/* Simone 08/07/2014 17:49 */

import core.AppProperties;
import core.Shutdown;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pgsql.ctl.*;

import java.io.File;

public class PgSqlStarter {

    public static void main(String[] args) {
        File appProp = new File("file.conf");
        PgSqlCtl pgsqlctl = new PgSqlCtl(new PgSqlConfigFix(), new AppProperties(appProp));
        PgSqlStarter pgSqlStarter = new PgSqlStarter(pgsqlctl);
        //pgsqlctl.stop();
        pgSqlStarter.start();
        pgSqlStarter.hookShutdown();

    }

    final Logger log = LoggerFactory.getLogger(getClass());
    private IPgSqlCtl pgSqlCtl;


    public PgSqlStarter(IPgSqlCtl pgSqlCtl) {
        this.pgSqlCtl = pgSqlCtl;
    }

    public void start() {
        if (!pgSqlCtl.dbExist()) {
            pgSqlCtl.initDb();
            pgSqlCtl.start();
        } else {
            CtlStatus status = pgSqlCtl.status();
            if (status == CtlStatus.NO_SERVER_RUNNING)
                pgSqlCtl.start();
            else if (status == CtlStatus.RUNNING)
                log.warn("PgSql already running");
        }
    }

    public void hookShutdown() {
        Shutdown.def().addApplicationShutdownHook(new Runnable() {
            @Override
            public void run() {
                log.info("Stopping PgSql");
                if (smartShutdownFailed()) {
                    if (pgSqlCtl.stopFast() == StopStatus.STOP_FAILED)
                        log.error("Unable to stop PgSql");
                }
            }
        });
    }

    private boolean smartShutdownFailed() {
        boolean result = pgSqlCtl.stop() == StopStatus.STOP_FAILED;
        if (result)
            log.warn("Smart shutdown failed. Issuing fast shutdown");
        return result;
    }
}
