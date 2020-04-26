package satd.step2

import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.slf4j.LoggerFactory
import satd.utils.*

fun main() {
    val log = LoggerFactory.getLogger("MainDbPostProcessing")
    log.info("starting")
    log.debug("starting")

    loglnStart("MainDbPostProcessing")

    config.load()

    persistence.setupDatabase()
    val rate = Rate(5000)
    val progress = AntiSpin(2000)
    fun log(msg: String) {
        logln("$msg Row done:${rate.spinCount} rows/sec:$rate")
    }

    //TODO load commits and issues count from RepoList file behind getGithubUrls()

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
    logln("DbRepos done")
    rate.reset()
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

    logln("Done")

}
