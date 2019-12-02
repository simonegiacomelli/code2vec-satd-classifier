package satd.utils

import java.net.URL
import kotlin.test.Test
import kotlin.test.assertEquals

class RepoTest {
    @Test
    fun testRepoFolder() {
        val target = Repo(URL("https://github.com/google/guava"))
        assertEquals("google", target.userName)
        assertEquals("guava", target.repoName)
        assertEquals("google_guava", target.friendlyName)
    }
}