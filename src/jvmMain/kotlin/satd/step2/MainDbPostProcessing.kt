package satd.step2

import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
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

    fun go() {
        loglnStart("MainDbPostProcessing")

        config.load()

        persistence.setupDatabase()

        task("importGithubUrlList") { importGithubUrlList() }
        task("updateDbSatdsFields") { updateDbSatdsFields() }

        logln("Done")
    }

    private fun task(title: String, act: () -> Unit) {
        logln("Starting $title")
        rate.reset()
        act()
        log("{$title} completed")
    }

    private fun importGithubUrlList() {
        transaction {
            DbRepos.apply {
                slice(id, url).select { commits.less(0).or(issues.less(0)) }.orderBy(id)
                    .forEach { row ->
                        try {
                            update({ DbRepos.id eq row[DbRepos.id] }) {
                                it[commits] = -2
                                it[issues] = -2
                            }
                            rate.spin()

                            progress.spin {
                                log("DbRepos")
                            }
                        } catch (ex: Exception) {
                            println("fault id ${row[DbSatds.id]}")
                            throw ex
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
                            rate.spin()
                            progress.spin {
                                log("DbSatds")
                            }
                        } catch (ex: Exception) {
                            println("fault id ${row[id]}")
                            throw ex
                        }
                    }
            }

        }
    }

    private fun log(msg: String) {
        logln("$msg Row done:${rate.spinCount} rows/sec:$rate")
    }

}
