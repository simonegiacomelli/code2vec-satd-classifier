package satd.step2

import satd.utils.*
import kotlin.streams.toList

fun main(args: Array<String>) {
    logln("Starting pid: $pid")
    config.load()
    HeapDumper.enable()
    Diagnosis.installALRM()

    persistence.setupDatabase()
    repoRate.startStatAsync()

    val pool = forkJoinPool()

    logln("Using pool: $pool")
    pool.submit {
        RepoList
            .androidReposFull
            .union(RepoList.androidReposFull2)
            .sorted()
            .also { repoRate.totRepo = it.size }
            .subtract(DbRepos.allDone().also { repoRate.alreadyDone(it.size) })
            .take(config.batch_size.toIntOrNull() ?: 1000)
            .stream()
            .parallel()
            .map { Repo(it).clone().reportFailed() }
            .filter { !it.failed }
            .map { Find(it).trackSatd() }
            .toList()
    }.get()
    logln("Done")

}
