package satd.step2

import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import satd.utils.*
import kotlin.streams.toList

fun main() {
    logln("Starting pid: $pid")
    config.load()

    persistence.setupDatabase()

    transaction {
        DbRepos.deleteWhere { DbRepos.success eq 0 }
    }

    logln("Done")

}
