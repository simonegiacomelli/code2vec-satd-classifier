package satd.step1

import java.net.URL
import java.util.concurrent.ForkJoinPool

fun main() {
    Main().go()
}

class Main {
    private fun repoUrlList() = this::class.java.classLoader.getResource("satd/step1/repo-urls.txt")!!

    fun go() {
        val threadCount = Runtime.getRuntime().availableProcessors()
        logln("Starting ${threadCount} threads")
        val customThreadPool = ForkJoinPool(threadCount)
        customThreadPool.submit {
            repoUrlList()
                .readText()
                .split('\n')
                .map { it.trim() }
                .filter { !it.startsWith("#") }
                .map { URL(it) }
                .parallelStream()
                .map { CloneRepo(it) }
                .forEach { it.ensureRepo() }
        }.get()
        logln("")
        logln("Clone done")
    }

}
