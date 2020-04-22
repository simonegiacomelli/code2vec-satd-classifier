package satd.step2

import org.h2.tools.Server
import org.jetbrains.exposed.dao.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import satd.utils.Folders
import satd.utils.RepoCsvRow
import satd.utils.logln
import java.io.PrintWriter
import java.io.StringWriter
import java.lang.IllegalArgumentException
import java.nio.file.Path
import java.nio.file.Paths
import java.sql.Connection
import java.sql.DriverManager
import org.h2.jdbcx.JdbcDataSource;
import javax.sql.DataSource

class Persistence(val databasePath: Path) {
    val url = "jdbc:h2:$databasePath;AUTO_SERVER=TRUE;AUTO_SERVER_PORT=19091"

    private val user = "sa"
    private val pass = ""

    fun connection(): Connection {
        Class.forName("org.h2.Driver")
        return DriverManager.getConnection(url, user, pass)
    }

    fun dataSource(): DataSource {
        val ds = JdbcDataSource()
        ds.setURL(url)
        ds.user = user
        ds.password = pass

        return ds
    }

    fun setupDatabase() {
        Database.connect(dataSource())
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
    //clean means: without comments
    val old_clean = text("old_clean")
    val new_clean = text("new_clean")
    val old_clean_len = integer("old_clean_len")
    val new_clean_len = integer("new_clean_len")
    val clean_diff_ratio = double("clean_diff_ratio")
    val code_hash = varchar("code_hash", 200).index(isUnique = true)
    val accept = integer("accept")
    val parent_count = integer("parent_count")
    val url = varchar("url", 400).default("")
    val old_clean_token_count = integer("old_clean_token_count").nullable()
    val new_clean_token_count = integer("new_clean_token_count").nullable()
    val inner_methods = integer("inner_methods").nullable()
    val valid = integer("valid").nullable()

    fun existsCodeHash(code_hash_str: String) = DbSatds.select { code_hash eq code_hash_str }.count() > 0
}

object DbRepos : LongIdTable() {
    val url = varchar("url", 200).index(isUnique = true)
    val success = integer("success").default(1)
    val done = integer("done").default(1)
    val module = varchar("module", 400).default("")
    val message = text("message").default("")
    val commits = integer("commits").default(-1)
    val sizeMB = integer("sizeMB").default(-1)

    fun allDone(): List<String> = transaction { slice(url).select { done eq 1 }.map { it[url] } }

    fun done(urlstr: String) {
        logln("$urlstr SUCCESS")
        repoRate.spin()
        transaction {
            if (select { (url eq urlstr) }.count() == 0)
                insert {
                    it[url] = urlstr
                    it[done] = 1
                }
            else
                update({ url eq urlstr }) {
                    it[done] = 1
                }
        }
    }

    fun failed(urlstr: String, ex: Throwable, modules: String) {
        val exstr = StringWriter().also { ex.printStackTrace(PrintWriter(it)) }.toString()
        logln("$urlstr FAILED $modules [${exstr.substringBefore('\n')}]")
        repoRate.spin()
        transaction(4, 0) {
            if (select { (url eq urlstr) }.count() == 0)
                insert {
                    it[url] = urlstr
                }
            update({ url eq urlstr }) {
                it[done] = 1
                it[success] = 0
                it[module] = modules
                it[message] = exstr
            }
        }
    }

    fun redoFailed() {
        transaction {
            update({ (done eq 1) and (success eq 0) }) {
                it[done] = 0
            }

        }
    }

    fun updateStats(row: RepoCsvRow) {
        transaction {
            if (select { (url eq row.url) }.count() == 0)
                insert {
                    it[url] = row.url
                    it[done] = 0
                    it[success] = -1
                    it[commits] = row.commits
                    it[sizeMB] = row.sizeMB
                }
            else
                update({ url eq row.url }) {
                    it[commits] = row.commits
                    it[sizeMB] = row.sizeMB
                }
        }
    }
}

//class DbSatd(id: EntityID<Long>) : LongEntity(id) {
//    companion object : LongEntityClass<DbSatd>(DbSatds)
//}


