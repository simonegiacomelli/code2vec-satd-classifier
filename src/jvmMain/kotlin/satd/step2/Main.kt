package satd.step2

import satd.utils.Repo
import satd.utils.RepoList
import satd.step1.*
import satd.utils.logln
import kotlin.streams.toList

fun main() {
    Main().go()
}

class Main {
    fun go() {
        logln("Starting")

        if (!Folders.database_db1.toFile().deleteRecursively())
            throw IllegalStateException("Errore removing the database ${Folders.database_db1}")

        RepoList
//            .tenRepos
            .androidRepos
            .stream()
            .parallel()
            .map { logln("Starting thread"); it }
            .map { Repo(it).clone() }
            .map { Find(it.newGit()).trackSatd() }
            .toList()

        logln("Done")
    }

}
