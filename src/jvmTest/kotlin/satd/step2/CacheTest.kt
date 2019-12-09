package satd.step2


import kotlin.test.*

class CacheTest {

    @Test
    fun verify() {
        val file = createTempFile("test-cache")
        val target = Cache(file)

        target["hello"] = "world"
        target.store()
        assertTrue { target.exists() }

        val t2 = Cache(file)
        t2.load()
        assertEquals("world", target["hello"])
    }
}