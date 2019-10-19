package satd.step1

import java.net.URL

fun repoUrlList() = RepoList.repoTxtResource()
    .readText()
    .split('\n')
    .map { it.trim() }
    .filter { !it.startsWith("#") }
    .map { URL(it) }

private class RepoList {
    companion object {
        fun repoTxtResource() = this::class.java.classLoader.getResource("satd/step1/repo-urls.txt")!!
        @JvmStatic
        fun main(args: Array<String>) {
            repoUrlList().forEach {
                println(it)
            }
        }
    }
}