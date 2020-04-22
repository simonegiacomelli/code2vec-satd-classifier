package pgsql.ctl

import core.FileUtils
import org.slf4j.LoggerFactory
import pgsql.DsPostgreSqlProvider
import pgsql.ctl.IPgSqlConfigFix
import java.nio.file.Path
import java.util.regex.Pattern

/* Simone 31/03/2015 17:00 */   class PgSqlConfigFix : IPgSqlConfigFix {
    val log = LoggerFactory.getLogger(javaClass)
    override fun fixConfig(dbPath: Path, tcpPort: Int) {
        val confFile = dbPath.resolve("postgresql.conf").toFile()
        val hbaFile = dbPath.resolve("pg_hba.conf").toFile()
        log.info("Configuring [{}]", confFile)
        FileUtils.append(confFile, "\nport=$tcpPort")
        FileUtils.append(confFile, "\nlisten_addresses = '*'")
        log.info("Configuring [{}]", hbaFile)
        //String hbaContent = StringUtils.replace(FileUtils.read(hbaFile), DsPostgreSqlProvider.USERNAME, "postgres") + "";

        var hbaContent = hbaFile.readText().replace( "postgres", DsPostgreSqlProvider.USERNAME);
        hbaContent = Pattern.compile(
            "(^host\\s+\\w+\\s+\\w+\\s+::1/128\\s+\\w+)",
            Pattern.MULTILINE
        ).matcher(hbaContent).replaceAll("#$1")
        FileUtils.write(
            hbaFile,
            "$hbaContent\nhost    all             all             samenet                 md5"
        )
    }
}