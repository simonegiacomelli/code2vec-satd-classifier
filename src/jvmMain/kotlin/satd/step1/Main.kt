package satd.step1

import java.net.URL
import java.util.stream.Collectors

fun main() {
    Main().go()
}

class Main {
    private fun repoUrlList() = this::class.java.classLoader.getResource("satd/step1/repo-urls.txt")!!

    fun go() {
        logln("Starting")
        repoUrlList()
            .readText()
            .split('\n')
            .map { it.trim() }
            .filter { !it.startsWith("#") }
            .map { URL(it) }
            .stream()
            .parallel()
            .map { logln("Starting thread"); it }
//            .also { logln("Starting thread") }
            .map { Repo(it).clone() }
            .map { Inspec(it).javaSources() }
            .map {  }
            .collect(Collectors.toList())

        logln("")
        logln("Clone done")
    }

}
