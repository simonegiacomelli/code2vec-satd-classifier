package satd.utils

import java.net.MalformedURLException
import java.net.URL

class RepoList {

    companion object {
        val tenRepos by lazy { repoUrlList("satd/step1/repo-urls.txt") }
        val testRepos by lazy { repoUrlList("satd/urls/test-repos.txt") }
        val androidReposFull by lazy { repoUrlList("satd/urls/android-repo-urls.txt") }
        val androidReposFull2 by lazy { repoUrlList("satd/urls/android-repo-urls2.txt") }
        val androidReposOOM by lazy { repoUrlList("satd/urls/android-repo-oom-urls.txt") }
        val androidReposStackOverflow by lazy { repoUrlList("satd/urls/android-repo-urls-stackoverflow.txt") }

        private fun repoTxtResource(resource: String): URL {
            return this::class.java.classLoader.getResource(resource)!!
        }

        private fun repoUrlList(resource: String): List<String> = repoTxtResource(resource)
            .readText()
            .split('\n')
            .map { it.trim() }
            .filter { !it.startsWith("#") }
            .filter { it.isNotEmpty() }
            .map {
                try {
                    URL(it)
                } catch (ex: MalformedURLException) {
                    throw Exception("Offending url [$it]", ex)
                }
                it
            }
    }
}

fun main(args: Array<String>) {
    RepoList.androidReposFull2
        .sorted()
        .forEach {
            println(it)
        }
}