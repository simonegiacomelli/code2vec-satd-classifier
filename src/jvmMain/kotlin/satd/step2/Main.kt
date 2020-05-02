package satd.step2

import satd.utils.*
import kotlin.streams.toList

fun main(args: Array<String>) {
    loglnStart("main")

    HeapDumper.enable()
    Diagnosis.installALRM()

    persistence.setupDatabase()
    repoRate.startStatAsync()

    repoRate.totRepo = DbRepos.totalCount()
    repoRate.alreadyDone(DbRepos.doneCount())

    val pool = forkJoinPool()
    logln("Using pool: $pool")
    pool.submit {
        DbRepos.allTodo()
            .take(config.batch_size.orEmpty().toIntOrNull() ?: 1000)
            .stream()
            .parallel()
            .map { Repo(it).clone().reportFailed() }
            .filter { it.toScan }
            .map { Find(it).scanSatd() }
            .toList()
    }.get()

    repoRate.logStat()
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