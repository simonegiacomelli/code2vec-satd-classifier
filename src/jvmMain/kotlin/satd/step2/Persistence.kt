package satd.step2

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

class Persistence {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            setupDatabase()
        }
    }
}

fun validationSql() = "CALL SESSION_ID()"

val databasePath get() = Folders.database.resolve("h2satd")

fun connection(): Connection {
    Class.forName("org.h2.Driver")
    return DriverManager.getConnection(
        "jdbc:h2:$databasePath;AUTO_SERVER=TRUE;AUTO_SERVER_PORT=19091",
        "sa",
        "sa"
    )
}


fun setupDatabase() {
    Database.connect(::connection)
    transaction {
        addLogger(StdOutSqlLogger)
        SchemaUtils.createMissingTablesAndColumns(DbSatds, DbFiles /* , AppLogs */)
    }
}

object DbSatds : LongIdTable() {
    val token = varchar("token", 38)
    val friendlyName = varchar("friendly_name", 100)
    val counterCalendarRequests = long("counter_calendar_requests")
    val counterUiRequests = long("counter_ui_requests")
    val dtCreation = datetime("dt_creation")
    val dtLastUse = datetime("dt_last_use").nullable()
    val dtLastCalendarRequests = datetime("dt_last_calendar_request").nullable()
    val dtLastUiRequests = datetime("dt_last_ui_request").nullable()
    val isAdmin = bool("is_admin").nullable()
}

class DbSatd(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<DbSatd>(DbSatds)

    var token by DbSatds.token
    var friendlyName by DbSatds.friendlyName
    var dtLastUse by DbSatds.dtLastUse
    var isAdmin by DbSatds.isAdmin
}

object DbFiles : LongIdTable() {
    val calendar = reference("calendar", DbSatds)
    val dtCreation = datetime("dt_creation")
    val summary = varchar("summary", 1000)
}

class DbFile(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<DbFile>(DbFiles)

    var calendar by DbSatd referencedOn DbFiles.calendar
    val summary by DbFiles.summary

}

//object AppLogs : LongIdTable() {
//    val calendar = reference("calendar", DbSatds).nullable()
//    val instance = varchar("instance", 30)
//    val dtCreation = datetime("dt_creation")
//    val kind = varchar("kind", 30)
//    val info = text("info")
//    val info2 = text("info2")
//}
//
//class AppLog(id: EntityID<Long>) : LongEntity(id) {
//    companion object : LongEntityClass<AppLog>(AppLogs)
//}

