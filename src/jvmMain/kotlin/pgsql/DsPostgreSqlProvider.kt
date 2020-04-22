package pgsql

import java.sql.Connection
import java.sql.SQLException
import java.sql.Statement

/* User: Simone 30/03/13 9.32 */

class DsPostgreSqlProvider {

    companion object {
        const val HOST = "localhost"
        const val NAME = "db"
        val USERNAME: String by lazy { System.getenv("USER") }
        const val PASSWORD = "usi"
    }

    fun init(pgConn: Connection) {
        try {
            val statement = pgConn.createStatement()
            if (dbNotExist(statement)) statement.execute(
                String.format(
                    "CREATE DATABASE %s WITH OWNER = %s ENCODING = 'UTF8' " +
                            "TABLESPACE = pg_default LC_COLLATE = 'C' LC_CTYPE = 'C' CONNECTION LIMIT = -1;"
                    , NAME, USERNAME
                )
            )
            statement.close()
        } catch (e: SQLException) {
            throw RuntimeException(e)
        }
    }

    private fun dbNotExist(statement: Statement): Boolean {
        val dbCountRs = statement.executeQuery("select count(*) from pg_database where datname='$NAME'")
        dbCountRs.next()
        val dbCount = dbCountRs.getInt(1)
        dbCountRs.close()
        return dbCount == 0
    } //

    //    private BasicDataSource getDataSource(String url, BasicDataSource ds) {
    //        ds.setUrl(url);
    //        ds.setDriverClassName("org.postgresql.Driver");
    //        ds.setUsername(getUser());
    //        ds.setPassword(getPassword());
    //        ds.setMaxWait(1000);
    //        ds.setMaxActive(16);
    //        return ds;
    //    }

}