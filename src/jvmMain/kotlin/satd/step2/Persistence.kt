package satd.step2

import org.h2.tools.Server
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.LongIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import satd.step1.Folders
import java.sql.Connection
import java.sql.DriverManager

fun main() {
    setupDatabase()
    Server.startWebServer(connection())
}

val databasePath get() = Folders.database_db1.resolve("h2satd")

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
        SchemaUtils.createMissingTablesAndColumns(DbSatds)
    }
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
}

class DbSatd(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<DbSatd>(DbSatds)
}
