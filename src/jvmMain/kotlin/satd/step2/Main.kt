package satd.step2

import satd.utils.*
import kotlin.streams.toList

fun main(args: Array<String>) {
    loglnStart("main")
    config.load()
    HeapDumper.enable()
    Diagnosis.installALRM()

    persistence.setupDatabase()
    repoRate.startStatAsync()

    val pool = forkJoinPool()

    logln("Using pool: $pool")
    pool.submit {
        RepoList
            .getGithubUrls()
            .sortedDescending()
            .also { repoRate.totRepo = it.size }
            .subtract(DbRepos.allDone().also { repoRate.alreadyDone(it.size) })
            .take(config.batch_size.toIntOrNull() ?: 1000)
            .stream()
            .parallel()
            .map { Repo(it).clone().reportFailed().also { repoRate.spin() } }
            .filter { !it.failed }
            .map { Find(it).trackSatd() }
            .toList()
    }.get()
    logln("Done")

}


object Verify1 {
    @JvmStatic
    fun main(args: Array<String>) {
        persistence.setupDatabase()
        val list = DbRepos.allDone().subtract(RepoList.getGithubUrls())
        println("Repositories that were done previously but that are not in the todo list")
        println("Count ${list.count()}")
        list.sorted().forEach {
            println(it)
        }

    }
}