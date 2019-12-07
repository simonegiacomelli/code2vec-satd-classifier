package satd.step2

import org.h2.tools.Server
import org.jetbrains.exposed.dao.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import satd.utils.Folders
import satd.utils.logln
import java.io.PrintWriter
import java.io.StringWriter
import java.lang.IllegalArgumentException
import java.nio.file.Path
import java.nio.file.Paths
import java.sql.Connection
import java.sql.DriverManager


class Persistence(val databasePath: Path) {

    fun connection(): Connection {
        Class.forName("org.h2.Driver")
        return DriverManager.getConnection(
            "jdbc:h2:$databasePath;AUTO_SERVER=TRUE;AUTO_SERVER_PORT=19091",
            "sa",
            ""
        )
    }

    fun setupDatabase() {
        Database.connect(::connection)
        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.createMissingTablesAndColumns(DbSatds, DbRepos)
        }
    }

    fun startWebServer() {
        Server.startWebServer(connection())
    }

}

val persistence = Persistence(Folders.database_db1.resolve("h2satd"))

fun main(args: Array<String>) {
    val p = if (args.isEmpty())
        persistence
    else {
        val databasePath = Paths.get(args.first())
        val fullPath = Paths.get(args.first() + ".mv.db")
        if (!fullPath.toFile().exists())
            throw IllegalArgumentException("Path [$fullPath] not found!")
        Persistence(databasePath)
    }
    p.startWebServer()
}


object DbSatds : LongIdTable() {
    val pattern = varchar("pattern", 200)
    val commit_message = text("commit_message")
    val old = text("old")
    val new = text("new")
    val commit = varchar("commit", 50)
    val repo = varchar("repo", 200)
    val old_len = integer("old_len")
    val new_len = integer("new_len")
    val old_clean = text("old_clean")
    val new_clean = text("new_clean")
    val old_clean_len = integer("old_clean_len")
    val new_clean_len = integer("new_clean_len")
    val clean_diff_ratio = double("clean_diff_ratio")
    val code_hash = varchar("code_hash", 200).index(isUnique = true)
    val accept = integer("accept")
    val parent_count = integer("parent_count")

    fun existsCodeHash(code_hash_str: String) = DbSatds.select { code_hash eq code_hash_str }.count() > 0
}

object DbRepos : LongIdTable() {
    val url = varchar("url", 200).index(isUnique = true)
    val success = integer("success").default(1)
    val module = varchar("module", 400).default("")
    val message = text("message").default("")
    fun allDone(): List<String> = transaction { slice(url).selectAll().map { it[url] } }
    fun done(urlstr: String) {
        transaction { DbRepos.insert { it[url] = urlstr } }
    }

    fun failed(urlstr: String, ex: Throwable, modules: String) {
        val exstr = StringWriter().also { ex.printStackTrace(PrintWriter(it)) }.toString()
        logln("$urlstr FAILED $modules [${exstr.substringBefore('\n')}]")
        transaction {
            DbRepos.insert {
                it[url] = urlstr
                it[success] = 0
                it[module] = modules
                it[message] = exstr
            }
        }
    }
}

//class DbSatd(id: EntityID<Long>) : LongEntity(id) {
//    companion object : LongEntityClass<DbSatd>(DbSatds)
//}


