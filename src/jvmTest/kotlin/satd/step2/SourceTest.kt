package satd.step2


import kotlin.test.Test
import kotlin.test.assertEquals


internal class SourceTest {
    val class1 by lazy { load("Class1.java") }
    val class2 by lazy { load("Class2.java") }
    val class2b by lazy { load("Class2b.java") }
    val class3 by lazy { load("Class3.java") }
    val class4 by lazy { load("Class4.java") }

    private fun load(s: String) = this::class.java.classLoader.getResource("satd/step2/$s")!!.readText()

    @Test
    fun `method with satd should be correctly detected`() {
        val target = Source(class1)
        assertEquals(1, target.satdList.size)
    }

    @Test
    fun `normal comment should be ignored`() {
        val target = Source(class2)
        assertEquals(0, target.satdList.size)
    }

    @Test
    fun `normal comment should be ignored b`() {
        val target = Source(class2b)
        assertEquals(0, target.satdList.size)
    }

    @Test
    fun `bait comment on field should be ignored`() {
        val target = Source(class3)
        assertEquals(0, target.satdList.size)
    }

    @Test
    fun `double satd comment in one method should be accounted as one`() {
        val target = Source(class4)
        assertEquals(1, target.satdList.size)
    }

    //TODO detect failed parsing of incorrect java source
}