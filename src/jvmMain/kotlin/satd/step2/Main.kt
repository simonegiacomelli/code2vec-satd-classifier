package satd.step2

import satd.utils.*
import kotlin.streams.toList

fun main(args: Array<String>) {
    logln("Starting")
    config.loadArgs(args)
    HeapDumper.enable()

    persistence.setupDatabase()

    RepoList
        .androidReposFull
        .also { Stat.totRepo = it.size }
        .subtract(DbRepos.allDone().also { Stat.repoDone.getAndSet(it.size) })
        .stream()
        .parallel()
        .map { Repo(it).clone() }
        .filter { !it.failed }
        .map { Find(it).trackSatd() }
        .toList()

    logln("Done")

}
