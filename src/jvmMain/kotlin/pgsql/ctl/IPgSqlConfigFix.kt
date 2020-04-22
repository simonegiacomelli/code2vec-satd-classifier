package pgsql.ctl

import java.nio.file.Path

/* Simone 31/03/2015 16:59 */  interface IPgSqlConfigFix {
    fun fixConfig(dbPath: Path, tcpPort: Int)
}