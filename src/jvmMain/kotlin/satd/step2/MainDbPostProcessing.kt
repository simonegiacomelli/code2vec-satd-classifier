package satd.step2

import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.slf4j.LoggerFactory
import pgsql.ctl.PgSqlCtl
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
    fun callbackp() {
        logln("Row done:${rate.spinCount} rows/sec:$rate")
    }
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

                        progress.spin(::callbackp)
                    } catch (ex: Exception) {
                        println("fault id ${row[id]}")
                        throw ex
                    }
                }
        }

    }

    logln("Done")

}
