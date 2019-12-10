package satd.step2

import satd.utils.*
import kotlin.streams.toList

fun main() {
    loglnStart("repoStats")
    config.load()

    repoRate.startStatAsync()

    val pool = forkJoinPool()

    RepoStatsFile.reset()

    logln("Using pool: $pool")
    pool.submit {
        RepoList
            .get()
            .also { repoRate.totRepo = it.size }
            .stream()
            .parallel()
            .map { Repo(it).stat(); }
            .toList()
    }.get()
    repoRate.logStat()

    logln("Done stats")

}

