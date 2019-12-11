package satd.step2

import satd.utils.*
import kotlin.streams.toList

fun main() {
    loglnStart("UpdateDbStats")
    logln("Starting pid: $pid")
    config.load()

    persistence.setupDatabase()

    repoRate.startStatAsync()

    val pool = forkJoinPool()

    logln("Using pool: $pool")
    pool.submit {
        RepoList
            .get()
            .also { repoRate.totRepo = it.size }
            .stream()
            .parallel()
            .map { DbRepos.updateStats(it); repoRate.spin() }
            .toList()
    }.get()

    repoRate.logStat()

    logln("Done")

}
