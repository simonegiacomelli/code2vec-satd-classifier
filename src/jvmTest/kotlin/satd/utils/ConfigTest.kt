package satd.utils

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class ConfigTest {

    @Test
    fun testLoadArgs() {
        val config = File(createTempDir(), "config.properties")
        config.writeText("repos_path=foo\nthread_count=123")
        val c = Config(config.parent)
        assertEquals("foo", c.repos_path)
        assertEquals("123", c.thread_count)
    }
}