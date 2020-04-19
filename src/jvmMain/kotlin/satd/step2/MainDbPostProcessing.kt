package satd.step2

import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import satd.utils.*

fun main() {
    loglnStart("MainDbPostProcessing")

    config.load()

    persistence.setupDatabase()

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
                            it[inner_methods] = if (
                                old.hasInnerMethods
                                || new.hasInnerMethods) 1 else 0
                        }
                    }catch (ex:Exception){
                        println("fault id ${row[id]}")
                        throw ex
                    }
                }
        }

    }

    logln("Done")

}
