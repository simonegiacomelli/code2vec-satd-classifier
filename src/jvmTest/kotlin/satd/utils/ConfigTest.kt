package satd.utils


import kotlin.test.Test
import kotlin.test.assertEquals

class ConfigTest {

    //@Test
    fun testLoadArgs() {
        config.loadArgs(arrayOf("-repo-urls", "satd/urls/android-repo-urls-stackoverflow.txt"))
    }
}