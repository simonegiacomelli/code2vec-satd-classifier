package satd.step2

import org.h2.jdbcx.JdbcDataSource
import org.h2.tools.Server
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.dao.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.postgresql.ds.PGSimpleDataSource
import pgsql.DsPostgreSqlProvider
import pgsql.PgSqlStarter
import satd.utils.RepoCsvRow
import satd.utils.logln
import java.io.PrintWriter
import java.io.StringWriter
import java.nio.file.Path
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import javax.sql.DataSource

interface IDb {
    fun connection(): Connection
    fun dataSource(): DataSource
    fun startDatabase()
    val url: String
}

class DbH2(databasePath: Path, h2_options: String = "AUTO_SERVER_PORT=19091") : IDb {
    override val url = "jdbc:h2:$databasePath;AUTO_SERVER=TRUE;$h2_options"

    private val user = "sa"
    private val pass = ""


    override fun connection(): Connection {
        Class.forName("org.h2.Driver")
        return DriverManager.getConnection(url, user, pass)
    }

    override fun dataSource(): DataSource = JdbcDataSource().also {
        it.setURL(url)
        it.user = user
        it.password = pass
    }

    override fun startDatabase() {}
}

class DbPgsql(
    val port: Int = DsPostgreSqlProvider.PORT
    , val databaseName: String = DsPostgreSqlProvider.NAME
    , val hostname: String = DsPostgreSqlProvider.HOST
    , val user: String = DsPostgreSqlProvider.USERNAME
    , val pass: String = DsPostgreSqlProvider.PASSWORD
) : IDb {
    init {
        Class.forName("org.postgresql.Driver")
    }

    override val url = "jdbc:postgresql://$hostname:$port/$databaseName"
    val urlMaster = "jdbc:postgresql://$hostname:$port/postgres"

    override fun connection(): Connection = connection(url)
    private fun connection(urlstr: String): Connection {
        return DriverManager.getConnection(urlstr, user, pass)
    }

    override fun dataSource(): DataSource = PGSimpleDataSource().also {
        it.portNumber = port
        it.serverName = hostname
        it.databaseName = databaseName
        it.user = user
        it.password = pass
    }

    override fun startDatabase() {
        PgSqlStarter.def.start()
        DsPostgreSqlProvider().init(connection(urlMaster))
    }
}


class Persistence(db: IDb) : IDb by db {

    fun setupDatabase() {
        logln("Jdbc url: [$url]")
        startDatabase()
        Database.connect(dataSource())
        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.createMissingTablesAndColumns(DbSatds, DbRepos, DbRuns, DbEvals)
        }
    }

    fun startWebServer() {
        Server.startWebServer(connection())
    }

}

//val persistence = Persistence(DbH2(Folders.database_db1.resolve("h2satd")))
val persistence = Persistence(DbPgsql())

fun main(args: Array<String>) {
    persistence.setupDatabase()
    persistence.startWebServer()
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

    fun duplicateCodeHash(code_hash_str: String) =
        slice(url, commit)
            .select { code_hash eq code_hash_str }
            .map { DuplicateSatd(it[url], it[commit]) }.firstOrNull()
}

data class DuplicateSatd(val url: String, val commit: String)

object DbRepos : LongIdTable() {
    val url = varchar("url", 200).index(isUnique = true)
    val success = integer("success").default(1)
    val done = integer("done").default(1)
    val module = varchar("module", 400).default("")
    val message = text("message").default("")
    val commits = integer("commits").default(-1)
    val sizeMB = integer("sizeMB").default(-1)
    val issues = integer("issues").default(-1)
    val created_at = varchar("created_at", 20).default("")

    fun allDone(): List<String> = transaction { slice(url).select { done eq 1 }.map { it[url] } }
    fun allTodo(): List<String> = transaction { slice(url).select { done eq 0 }.map { it[url] } }

    fun totalCount(): Int = transaction { selectAll().count() }
    fun doneCount(): Int = transaction { select { done eq 1 }.count() }

    fun done(urlstr: String, exstr: String = "") {
        logln("$urlstr SUCCESS")
        repoRate.spin()
        transaction {
            if (select { (url eq urlstr) }.count() == 0)
                insert {
                    it[url] = urlstr
                    it[done] = 1
                    it[success] = 1
                    it[message] = exstr
                }
            else
                update({ url eq urlstr }) {
                    it[done] = 1
                    it[success] = 1
                    it[message] = exstr
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
object DbRuns : IntIdTable() {
    /*
        SELECT run_id, count(*) *2 , sum(cast(satd_ok+fixed_ok as decimal)) / (count(*)*2) FROM dbevals
        group by run_id

        SELECT s.pattern, satd_ok,fixed_ok, count(*) from dbevals  e join dbsatds s on (s.id = e.satd_id)
        where e.run_id = 1
        group by s.pattern, satd_ok, fixed_ok
        order by s.pattern, satd_ok,fixed_ok


    //export run in csv file
    //link al commit r.url || '/commit/' || s.commit
        COPY (
        SELECT e.*,
       r.url || '/commit/' || s.commit AS commit_url,
       r.*,
       s.*
FROM dbevals e
  JOIN dbsatds s ON (s.id = e.satd_id)
  JOIN dbrepos r ON (r.url = s.url)
WHERE e.run_id = 1
ORDER BY s.pattern,
         satd_ok,
         fixed_ok


    ) TO '/tmp/satd-classfier-run-1.csv' CSV HEADER;

    */
    val create_time = datetime("create_time")
    val total_count = integer("total_count")
    val correct_count = integer("correct_count")
    val accuracy = double("accuracy")
    val train_count = integer("train_count")
    val validation_count = integer("validation_count")
    val test_count = integer("test_count")
    val hostname = varchar("hostname", 50).default("")
    val username = varchar("username", 50).default("")
    val satds_where = text("satds_where")
    val train_ids = text("train_ids")
    val validation_ids = text("validation_ids")
    val test_ids = text("test_ids")

    fun newRun(datasetInfo: DatasetInfo, result: Result): Int = transaction {
        val ins = insertAndGetId { row ->
            row[total_count] = result.totalCount
            row[correct_count] = result.correctCount
            row[accuracy] = result.accuracy
            row[create_time] = DateTime()
            row[train_count] = datasetInfo.trainCount
            row[validation_count] = datasetInfo.validationCount
            row[test_count] = datasetInfo.testCount
            row[satds_where] = datasetInfo.where
            row[train_ids] = datasetInfo.satdIdsToString(Partitions.training)
            row[validation_ids] = datasetInfo.satdIdsToString(Partitions.validation)
            row[test_ids] = datasetInfo.satdIdsToString(Partitions.test)
            row[username] = satd.utils.username
            row[hostname] = satd.utils.hostname
        }
        ins.value
    }
}

object DbEvals : LongIdTable() {
    val run_id = integer("run_id").references(DbRuns.id)
    val satd_id = long("satd_id").references(DbSatds.id)
    val satd_ok = short("satd_ok")
    val fixed_ok = short("fixed_ok")
    val satd_confidence = double("satd_confidence")
    val fixed_confidence = double("fixed_confidence")
}

/*
create table DbDups(
id serial primary key,
satd_id int8,
kind char(1),
hash char(32))


create index dbdups1 on dbdups (hash)

 */
fun ResultSet.toSequence(): Sequence<Array<Any>> = sequence {
    while (next()) {
        yield((1..metaData.columnCount).map { getObject(it) }.toTypedArray())
    }
}

fun Connection.query(sql: String) = prepareStatement(sql).executeQuery().toSequence()


