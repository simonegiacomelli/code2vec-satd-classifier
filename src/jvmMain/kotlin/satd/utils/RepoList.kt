package satd.utils

import java.net.MalformedURLException
import java.net.URL

class RepoList {

    companion object {
        val tenRepos by lazy { repoUrlList("satd/step1/repo-urls.txt") }
        val androidReposFull by lazy { repoUrlList("satd/urls/android-repo-urls.txt") }
        val androidRepos100 by lazy { repoUrlList("satd/urls/android-repo-urls-100.txt") }


        private fun repoTxtResource(resource: String): URL {

            return this::class.java.classLoader.getResource(resource)!!
        }

        private fun repoUrlList(resource: String) = repoTxtResource(resource)
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
            }

        @JvmStatic
        fun main(args: Array<String>) {
            androidReposFull.forEach {
                println(it)
            }
        }
    }
}