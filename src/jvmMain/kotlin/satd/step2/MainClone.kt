package satd.step2

import satd.utils.*
import kotlin.streams.toList
import kotlin.system.measureTimeMillis
import kotlin.time.measureTime

fun main() {
    loglnStart("clone")
    config.load()
    HeapDumper.enable()

    repoRate.startStatAsync()
    val take = RepoList
        .getGithubUrls()
        .take(20)
    println("going to clone the following repos")
    take.forEach {
        logln("git clone --no-checkout $it")
        Repo(it).apply {
            folder.deleteRecursively()
            integrityMarker.delete()
        }


    }
    logln("removed previous folders")
    logln("starting clone")

    measureTimeMillis {

        take
            .also { repoRate.totRepo = it.size }
            .map { Repo(it).clone(); repoRate.spin() }
            .toList()

    }.also {
        logln("took ${it / 1000} seconds")
    }
    logln("Done cloning")

}

