package satd.step2


import kotlin.test.Test
import kotlin.test.assertEquals


internal class SourceTest {
    fun class1() = this::class.java.classLoader.getResource("satd/step2/Class1.java")!!

    @Test
    fun a() {
        val target = Source(class1().readText())
        assertEquals(1, target.satd.size)
    }
}