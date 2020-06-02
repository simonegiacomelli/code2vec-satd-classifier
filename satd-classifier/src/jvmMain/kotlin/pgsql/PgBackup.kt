package pgsql

import satd.step2.DbPgsql
import satd.step2.assert2
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
        DbPgsql(databaseName = DsPostgreSqlProvider.NAME).startDatabase()
        assert2(file.exists()) { "Folder does not exists! $file" }
        PgSqlStarter.def.pgSqlCtl.pg_restore(DsPostgreSqlProvider.NAME, file.absolutePath)
        println("PgRestore end")
    }
}

object PgRemoveDataFolder {
    @JvmStatic
    fun main(args: Array<String>) {
        val folder = File(PgDefaults.dataFolder).absoluteFile
        println("This will delete the data folder of postgres $folder")
        println("Do you want to continue? Type y and enter to continue. Anything else and enter to exit")
        if (readLine() == "y") {
            PgSqlStarter.def.pgSqlCtl.stopFast()
            folder.deleteRecursively()
        }
        println("PgRemoveDataFolder end")
    }
}