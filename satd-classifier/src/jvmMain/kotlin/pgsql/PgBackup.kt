package pgsql

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import com.xenomachina.argparser.mainBody
import pgsql.ctl.PgSqlCtl
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

private class Args(parser: ArgParser) {
    val data_folder by parser.storing(
        "--data_folder",
        help = "postgres data folder default: ${PgDefaults.dataFolder}"
    ).default(PgDefaults.dataFolder)

    val username by parser.storing(
        "--username",
        help = "username"
    ).default(DsPostgreSqlProvider.USERNAME)

    val password by parser.storing(
        "--password",
        help = "password"
    ).default(DsPostgreSqlProvider.PASSWORD)

    val bin_folder by parser.storing(
        "--bin_folder",
        help = "postgres binaries folder. default: ${PgDefaults.binFolder}"
    ).default(PgDefaults.binFolder)
    val tcp_port by parser.storing(
        "--tcp_port",
        help = "tcp listening port. default: ${PgDefaults.tcpPort}"
    ) { toInt() }.default(PgDefaults.tcpPort)

    companion object {
        operator fun invoke(args: Array<String>) = ArgParser(args).parseInto(::Args).run {
            PgSqlCtl(
                pgsqlDataFolder = data_folder,
                pgsqlTcpPort = tcp_port,
                pgsqlBinFolder = bin_folder,
                pgsqlUsername = username,
                pgsqlPassword = password
            )
        }
    }
}

object PgBackup2 {
    @JvmStatic
    fun main(args: Array<String>) {
        PgSqlStarter(Args(args)).start()
        file.deleteRecursively()
        file.parentFile.mkdirs()
        Args(args).pg_dump(DsPostgreSqlProvider.NAME, file.absolutePath)
    }
}

object PgRestore2 {
    @JvmStatic
    fun main(args: Array<String>) {
        assert2(file.exists()) { "Folder does not exists! $file" }
        Args(args).also {
            PgSqlStarter(it).start()
            it.toDbPgsql().init()
            it.pg_restore(DsPostgreSqlProvider.NAME, file.absolutePath)
        }
        println("PgRestore end")
    }
}

object PgStartInstance {
    @JvmStatic
    fun main(args: Array<String>) = mainBody {
        PgSqlStarter(Args(args)).start(hookShutdown = false)
        println("PgStartInstance end")
    }
}

object PgStopInstance {
    @JvmStatic
    fun main(args: Array<String>) = mainBody {
        Args(args).stop()
        println("PgStopInstance end")
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
