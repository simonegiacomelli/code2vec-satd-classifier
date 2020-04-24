package pgsql

import java.io.File

val file by lazy { File("./data/backup/bk1") }

object PgBackup {
    @JvmStatic
    fun main(args: Array<String>) {
        PgSqlStarter.def.start()
        file.deleteRecursively()
        file.parentFile.mkdirs()
        PgSqlStarter.def.pgSqlCtl.pg_dump(DsPostgreSqlProvider.NAME, file.absolutePath)
    }
}

object PgRestore {
    @JvmStatic
    fun main(args: Array<String>) {
        PgSqlStarter.def.start()
        PgSqlStarter.def.pgSqlCtl.pg_restore(DsPostgreSqlProvider.NAME, file.absolutePath)
    }
}

object PgRemoveDataFolder {
    @JvmStatic
    fun main(args: Array<String>) {
        PgSqlStarter.def.pgSqlCtl.stopFast()
        File(PgDefaults.dataFolder).deleteRecursively()
    }
}