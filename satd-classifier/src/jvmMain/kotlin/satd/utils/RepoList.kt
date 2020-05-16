package satd.utils

import java.net.MalformedURLException
import java.net.URL
import kotlin.streams.toList

class RepoList {

    companion object {
        val tenRepos by lazy { repoUrlList("satd/urls/repo-urls.txt") }
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
                checkUrl(it)
                it
            }

        private fun checkUrl(url: String) {
            try {
                URL(url)
            } catch (ex: MalformedURLException) {
                throw Exception("Offending url [$url]", ex)
            }
        }

        fun getGithubUrls(): List<String> {
            return repoTxtResource("satd/urls/github_mining/commit100_or_issue100/github-url-list.txt")
                .openStream().use { inStream ->
                    inStream.bufferedReader()
                        .lines()
                        .filter { !it.startsWith("#") }
                        .filter { it.isNotEmpty() }
                        .map { it.split("\t") }
                        .sorted { t, t2 -> t[0].compareTo(t2[0]) }
                        .map { "https://github.com/${it[1]}" }
                        .toList()
                }
        }

        fun getGithubUrlsExtended(): List<Array<String>> {
            return repoTxtResource("satd/urls/github_mining/commit100_or_issue100/github-url-list.txt")
                .openStream().use { inStream ->
                    inStream.bufferedReader()
                        .lines()
                        .filter { !it.startsWith("#") }
                        .filter { it.isNotEmpty() }
                        .map { it.split("\t") }
                        .sorted { t, t2 -> t[0].compareTo(t2[0]) }
                        .map { arrayOf(it[0], "https://github.com/${it[1]}", it[2], it[3]) }
                        .toList()
                }
        }


        fun get() = csv("satd/urls/android-50-thousand.csv")
        fun getUrls() = get().sortedBy { it.commits }.map { it.url }

        fun csv(resource: String): List<RepoCsvRow> {
            return repoTxtResource(resource)
                .readText()
                .split("\n")
                .drop(1)
                .filter { !it.startsWith("#") }
                .filter { it.isNotEmpty() }
                .map {
                    val p = it.split(",")
                    checkUrl(p[0])
                    val row = RepoCsvRow(p[0])
                    row.commits = p[1].toInt()
                    row.sizeMB = p[2].toInt()
                    row
                }
        }
    }
}

data class RepoCsvRow(val url: String) {
    var commits: Int = -1
    var sizeMB: Int = -1
}