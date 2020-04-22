package pgsql.ctl;

/* Simone 08/07/2014 11:54 */

public interface IPgSqlCtl {

    CtlStatus status();

    StopStatus stop();
    StopStatus stopFast();

    void start();

    void initDb();

    boolean dbExist();

}
