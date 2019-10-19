package satd.step1

import java.net.URL

fun repoUrlList() = RepoList.repoUrlList()

private class RepoList {
    companion object {
        fun repoTxtResource() = this::class.java.classLoader.getResource("satd/step1/repo-urls.txt")!!

        fun repoUrlList() = repoTxtResource()
            .readText()
            .split('\n')
            .map { it.trim() }
            .filter { !it.startsWith("#") }
            .map { URL(it) }

        @JvmStatic
        fun main(args: Array<String>) {
            repoUrlList().forEach {
                println(it)
            }
        }
    }
}