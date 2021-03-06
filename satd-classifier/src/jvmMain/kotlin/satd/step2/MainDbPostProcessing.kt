package satd.step2

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import satd.utils.*
import kotlin.concurrent.getOrSet

fun main() {
    DbPostProcessing().go()
}

class DbPostProcessing {
    val log = LoggerFactory.getLogger("MainDbPostProcessing")
    val rate = Rate(5000)
    val progress = AntiSpin(2000)
    val current = StringBuilder()
    fun go() {
        loglnStart("MainDbPostProcessing")
        persistence.setupDatabase()
        task("detectCodeDuplicatesAndUpdateAccept") { executeAllStatements(detectCodeDuplicatesAndUpdateAccept) }
        task("extractJavaFeatures") { extractJavaFeatures() }
        task("detectFeaturesDuplicatesAndUpdateAccept") { executeAllStatements(detectFeaturesDuplicatesAndUpdateAccept) }
        task("updateDbSatdsFields") { updateDbSatdsFields() }
        logln("Done")
    }

    private fun executeAllStatements(sqlList: String) {
        transaction {
            sqlList
                .split(";")
                .filter { it.trim().isNotEmpty() }
                .forEach { sql ->
                    logln("executing ${sql.trim()}")
                    connection
                        .createStatement()
                        .use {
                            val recordCount = it.executeUpdate(sql)
                            logln("    Record affected: $recordCount")

                        }
                }
        }
    }

    private fun task(title: String, act: () -> Unit) {
        current.clear()
        current.append(title)
        rate.reset()
        logln("Starting $title")
        act()
        logRate(title, postMsg = " COMPLETE")
        rate.reset()
    }

    @Synchronized
    private fun spin() {
        rate.spin()
        progress.spin {
            logRate(current.toString())
        }
    }

    private fun importGithubUrlList() {
        RepoList
            .getGithubUrlsExtended()
            .chunked(1000)
            .forEach { chunk ->
                transaction {
                    chunk.forEach { row ->
                        val createdAt = row[0]
                        val urlStr = row[1]
                        val issueCount = row[2].toInt()
                        val commitCount = row[3].toIntOrNull() ?: -3


                        DbRepos.apply {
                            fun UpdateBuilder<Number>.common() {
                                val it = this
                                it[created_at] = createdAt
                                it[issues] = issueCount
                                it[commits] = commitCount
                            }
                            try {
                                if (select { (url eq urlStr) }.count() == 0)
                                    insert { it ->
                                        it[url] = urlStr
                                        it[done] = 0
                                        it[success] = 0
                                        it.common()
                                    }
                                else
                                    update({ url eq urlStr }) { it ->
                                        it.common()
                                    }
                                spin()
                            } catch (ex: Exception) {
                                logln("fault id $urlStr")
                                throw ex
                            }
                        }
                    }
                }
            }
    }

    private fun updateDbSatdsFields() {
        transaction {
            DbSatds.apply {
                selectAll().orderBy(id)
                    .forEach { row ->
                        try {
                            update({ id eq row[id] }) {
                                val old = JavaMethod(row[old_clean])
                                val new = JavaMethod(row[new_clean])
                                it[old_clean_token_count] = old.tokenCount
                                it[new_clean_token_count] = new.tokenCount
                                it[valid] = if (old.valid && new.valid) 1 else 0
                            }
                            spin()
                        } catch (ex: Exception) {
                            logln("fault id ${row[id]}")
                            throw ex
                        }
                    }
            }

        }
    }

    private fun extractJavaFeatures() {

        val extractService = ThreadLocal<ExtractService>()

        fun ext(id: String, code: String, header: String): String {
            val message = extractService.getOrSet { ExtractService() }.extract(id, code)
            val p = message.split("\t", limit = 2)
            return message
        }

        val pool = forkJoinPool()
        logln("Using pool: $pool")

        transaction {
            DbSatds.apply {
                val todo = accept.eq(1).and(old_clean_features.eq("").or(new_clean_features.eq("")))
                println("Extracting features for ${slice().select { todo }.count()} rows")
                slice(id, old_clean, new_clean).select { todo }.orderBy(id).asSequence().chunked(2000)
                    .forEach { chunk ->
                        pool.submit {
                            chunk.chunked(100).stream().parallel().forEach { rows ->
                                transaction {
                                    rows.forEach { row ->
                                        val id = row[id].value.toString()
                                        try {
                                            update({ DbSatds.id eq row[DbSatds.id] }) {
                                                it[old_clean_features] = ext(id, row[old_clean], "$id old")
                                                it[new_clean_features] = ext(id, row[new_clean], "$id new")
                                            }
                                            spin()
                                        } catch (ex: Exception) {
                                            logln("fault id $id")
                                            throw ex
                                        }
                                    }
                                }
                            }
                        }.get()
                    }
            }

        }
    }

    private fun logRate(msg: String, postMsg: String = "") {
        logln("$msg Row done:${rate.spinCount} rows/sec:$rate$postMsg")
    }

}


val detectCodeDuplicatesAndUpdateAccept = """
    
    drop table if exists DbDups;
    create table DbDups(
    id serial primary key,
    satd_id int8,
    kind char(1),
    hash char(32));

    insert into dbdups (satd_id,kind,hash)  select id,'s',md5(old_clean) from dbsatds;
    insert into dbdups (satd_id,kind,hash)  select id,'f',md5(new_clean) from dbsatds;


    create index dbdups1 on dbdups (hash);

    drop table if exists bad_ids ;
    select s.satd_id s_id,f.satd_id f_id into temp bad_ids from dbdups s join dbdups f on (s.hash = f.hash and s.kind='s' and f.kind='f' );

    update DbSatds set ${DbSatds.accept.name} = 1;
    update DbSatds set ${DbSatds.accept.name} = -1 where id in (select s_id from bad_ids union select f_id from bad_ids);
    update DbSatds set ${DbSatds.accept.name} = -1 where old_clean_token_count > 10000 or new_clean_token_count > 10000;
""".trimIndent()

val detectFeaturesDuplicatesAndUpdateAccept = """
    
    update DbSatds set ${DbSatds.accept.name} = 0 where (SUBSTRING(old_clean_features,1,1) = 'F' OR SUBSTRING(new_clean_features,1,1) = 'F');
    
    drop table if exists DbDups;
    create table DbDups(
    id serial primary key,
    satd_id int8,
    kind char(1),
    hash char(32));


    insert into dbdups (satd_id,kind,hash)  select id,'s',md5(old_clean_features) from dbsatds where ${DbSatds.accept.name} = 1;
    insert into dbdups (satd_id,kind,hash)  select id,'f',md5(new_clean_features) from dbsatds where ${DbSatds.accept.name} = 1;


    create index dbdups1 on dbdups (hash);

    drop table if exists bad_ids ;
    select s.satd_id s_id,f.satd_id f_id into temp bad_ids from dbdups s join dbdups f on (s.hash = f.hash and s.kind='s' and f.kind='f' );

    update DbSatds set ${DbSatds.accept.name} = -2 where id in (select s_id from bad_ids union select f_id from bad_ids);
""".trimIndent()

/*
better alternative from above
    drop table if exists DbDups;
    create table DbDups(
    id serial primary key,
    satd_id int8,
    kind char(1),
    hash char(32));


    insert into dbdups (satd_id,kind,hash)  select id,'s',md5(substring(old_clean_features, position( ' ' in substring( old_clean_features, 4) ) +4 )) from dbsatds ;
    insert into dbdups (satd_id,kind,hash)  select id,'f',md5(substring(new_clean_features, position( ' ' in substring( new_clean_features, 4) ) +4 )) from dbsatds ;


    create index dbdups1 on dbdups (hash);

    drop table if exists bad_ids ;
    select s.satd_id s_id,f.satd_id f_id into temp bad_ids from dbdups s join dbdups f on (s.hash = f.hash and s.kind='s' and f.kind='f' );

   select * from bad_ids;

  select * from dbsatds where id in (select s_id from bad_ids)
 */