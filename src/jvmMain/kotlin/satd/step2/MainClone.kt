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
        println("git clone --no-checkout $it")
        Repo(it).apply {
            folder.deleteRecursively()
            integrityMarker.delete()
        }


    }
    println("removed previous folders")
    println("starting clone")

    measureTimeMillis {

        take
            .also { repoRate.totRepo = it.size }
            .map { Repo(it).clone(); repoRate.spin() }
            .toList()

    }.also {
        println("took ${it / 1000} seconds")
    }
    logln("Done cloning")

}

