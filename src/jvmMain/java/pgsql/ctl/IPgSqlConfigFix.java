package pgsql.ctl;

/* Simone 31/03/2015 16:59 */

import java.nio.file.Path;

public interface IPgSqlConfigFix {
    void fixConfig(Path dbPath, int tcpPort);
}
