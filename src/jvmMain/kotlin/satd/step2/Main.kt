package satd.step2

import satd.utils.*
import java.util.concurrent.ForkJoinPool
import kotlin.streams.toList

fun main(args: Array<String>) {
    logln("Starting")
    config.loadArgs(args)
    HeapDumper.enable()

    persistence.setupDatabase()
    val pool = ForkJoinPool(
        config.thread_count.toIntOrNull() ?: 10,
        { pool: ForkJoinPool? ->
            val w = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool)
            w.name = "w" + w.poolIndex.toString().padStart(2, '0')
            w
        }, null, false
    )


    logln("Using pool: $pool")
    pool.submit {
        RepoList
            .androidReposFull
            .union(RepoList.androidReposFull2)
            .sorted()
            .also { Stat.totRepo = it.size }
            .subtract(DbRepos.allDone().also { Stat.repoDone.getAndSet(it.size) })
            .take(10)
            .stream()
            .parallel()
            .map { Repo(it).clone() }
            .filter { !it.failed }
            .map { Find(it).trackSatd() }
            .toList()
    }.get()
    logln("Done")

}
