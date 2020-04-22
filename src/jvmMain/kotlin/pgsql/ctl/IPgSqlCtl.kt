package pgsql.ctl

/* Simone 08/07/2014 11:54 */
interface IPgSqlCtl {
    fun status(): CtlStatus
    fun stop(): StopStatus
    fun stopFast(): StopStatus
    fun start()
    fun initDb()
    fun dbExist(): Boolean
}