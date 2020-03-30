package satd.step2

import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import satd.utils.*

fun main() {
    loglnStart("MainDbPostProcessing")
    logln("Starting pid: $pid")
    config.load()

    persistence.setupDatabase()

    transaction {
        DbSatds.apply {
            selectAll().orderBy(id)
                .forEach { row ->
                    update({ id eq row[id] }) {
                        it[old_clean_token_count] = JavaMethod(row[old_clean]).tokenCount
                        it[new_clean_token_count] = JavaMethod(row[new_clean]).tokenCount
                    }
                }
        }

    }

    logln("Done")

}
