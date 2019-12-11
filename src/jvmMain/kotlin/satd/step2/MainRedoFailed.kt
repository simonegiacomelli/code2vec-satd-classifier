package satd.step2

import satd.utils.config
import satd.utils.logln
import satd.utils.pid

fun main() {
    logln("Starting pid: $pid")
    config.load()

    persistence.setupDatabase()

    DbRepos.redoFailed()

    logln("Done")

}
