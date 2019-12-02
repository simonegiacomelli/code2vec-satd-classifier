package satd.step2

import satd.utils.*
import kotlin.streams.toList

fun main() {
    logln("Starting")
    HeapDumper.enable()

    if (!Folders.database_db1.toFile().deleteRecursively())
        throw IllegalStateException("Errore removing the database ${Folders.database_db1}")

    RepoList
        .androidReposFull
//            .take(2000)
        .stream()
        .parallel()
        .map { Repo(it).clone() }
        .filter { !it.failed }
        .map { Find(it.newGit()).trackSatd() }
        .toList()

    logln("Done")

}

