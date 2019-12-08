package satd.utils

import kotlin.test.Test
import kotlin.test.assertEquals

class RepoTest {
    @Test
    fun testRepoFolder() {
        config.load()
        val target = Repo("https://github.com/google/guava")
        assertEquals("google", target.userName)
        assertEquals("guava", target.repoName)
        assertEquals("google_guava", target.friendlyName)
    }
}