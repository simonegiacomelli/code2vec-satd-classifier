package satd.step2

import satd.utils.*
import kotlin.streams.toList

fun main(args: Array<String>) {
    logln("Removing checkout")
    config.load()
    HeapDumper.enable()

    repoRate.startStatAsync()

    val pool = forkJoinPool()

    logln("Using pool: $pool")
    pool.submit {
        RepoList
            .androidReposFull
            .union(RepoList.androidReposFull2)
            .sorted()
            .also { repoRate.totRepo = it.size }
            .stream()
            .parallel()
            .map { Repo(it).removeCheckout(); repoRate.spin() }
            .toList()
    }.get()

    logln("Done removing checkout")

}

