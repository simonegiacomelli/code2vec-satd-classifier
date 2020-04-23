package pgsql

import java.io.File

fun main() {
    PgSqlStarter.def.start()
    val file = File("./bk1")
    file.deleteRecursively()
    PgSqlStarter.def.pgSqlCtl.pg_dump("db", file.absolutePath)
}