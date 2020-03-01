package satd.step2

import satd.utils.*
import kotlin.streams.toList

fun main() {
    loglnStart("clone")
    config.load()
    HeapDumper.enable()

    repoRate.startStatAsync()

    val pool = forkJoinPool()

    logln("Using pool: $pool")
    pool.submit {
        RepoList
            .getGithubUrls()
            .also { repoRate.totRepo = it.size }
            .stream()
            .parallel()
            .map { Repo(it).clone(); repoRate.spin() }
            .toList()
    }.get()

    logln("Done cloning")

}

