package satd.step2

import satd.utils.*
import kotlin.streams.toList

fun main(args: Array<String>) {
    logln("Starting")
    config.loadArgs(args)
    HeapDumper.enable()

    if (!Folders.database_db1.toFile().deleteRecursively())
        throw IllegalStateException("Errore removing the database ${Folders.database_db1}")

    val urls = RepoList
        .androidReposFull
//            .take(2000)
    Stat.totRepo = urls.size
    urls
        .stream()
        .parallel()
        .map { Repo(it).clone() }
        .filter { !it.failed }
        .map { Find(it).trackSatd() }
        .toList()

    logln("Done")

}
