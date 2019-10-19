package satd.step1

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
//            .also { logln("Starting thread") }
            .map { Repo(it).clone() }
            .map { Inspec(it).javaSources() }
            .map { it.satdToFile(); it }
            .toList()

        logln("Clone done")
        logln("You can find the generated output in folder ${Folders.satd.normalize().toAbsolutePath()}")
    }

}
