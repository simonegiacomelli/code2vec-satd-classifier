package satd.step2

import satd.step1.Folders
import satd.step1.Repo
import satd.step1.logln
import satd.step1.repoUrlList
import java.util.stream.Collectors
import kotlin.streams.toList

fun main() {
    Main().go()
}

class Main {
    fun go() {
        logln("Starting")

        repoUrlList()
            .stream()
            .parallel()
            .map { logln("Starting thread"); it }
            .map { Repo(it).clone() }
            .map { Find(it.newGit()).trackSatd() }
            .toList()

        logln("Clone done")
        logln("You can find the generated output in folder ${Folders.satd.normalize().toAbsolutePath()}")
    }

}
