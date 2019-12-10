package satd.step2

import satd.utils.*
import kotlin.streams.toList

fun main() {
    logln("Starting repo stat")
    config.load()

    repoRate.startStatAsync()

    val pool = forkJoinPool()

    RepoStatsFile.reset()

    logln("Using pool: $pool")
    pool.submit {
        RepoList
            .androidReposFull
            .union(RepoList.androidReposFull2)
            .sorted()
            .also { repoRate.totRepo = it.size }
            .stream()
            .parallel()
            .map { Repo(it).stat(); repoRate.spin() }
            .toList()
    }.get()

    logln("Done stats")

}

