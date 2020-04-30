package satd.step2

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.slf4j.LoggerFactory
import satd.utils.*

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

        config.load()

        persistence.setupDatabase()

        task("importGithubUrlList") { importGithubUrlList() }
        task("updateDbSatdsFields") { updateDbSatdsFields() }

        logln("Done")
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
                chunk.forEach { row ->
                    val createdAt = row[0]
                    val urlStr = row[1]
                    val issueCount = row[2].toInt()
                    val commitCount = row[3].toIntOrNull() ?: -3

                    transaction {
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

    private fun logRate(msg: String, postMsg: String = "") {
        logln("$msg Row done:${rate.spinCount} rows/sec:$rate$postMsg")
    }

}

