package satd.step2

import satd.utils.Repo
import satd.utils.RepoList
import satd.step1.*
import satd.utils.HeapDumper
import satd.utils.logln
import kotlin.streams.toList

fun main() {
    logln("Starting")
    HeapDumper.enable()

    if (!Folders.database_db1.toFile().deleteRecursively())
        throw IllegalStateException("Errore removing the database ${Folders.database_db1}")

    RepoList
        .androidReposStackOverflow
//            .take(2000)
        .stream()
        .parallel()
        .map { Repo(it).clone() }
        .filter { !it.failed }
        .map { Find(it.newGit()).trackSatd() }
        .toList()

    logln("Done")

}
