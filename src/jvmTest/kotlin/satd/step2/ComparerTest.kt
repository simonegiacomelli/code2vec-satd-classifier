package satd.step2

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class ComparerTest {
    val class1 by lazy { load("Class1.java") }
    val class2 by lazy { load("Class2.java") }

    private fun load(s: String) = this::class.java.classLoader.getResource("satd/step2/ComparerTest/$s")!!.readText()

    @Test
    fun `x`() {
        val c1list = findMethodsWithSatd(class1)
        val c2list = findMethodsByName(class2, setOf("method1"))
        assertEquals(1, c1list.size)
        assertEquals(1, c2list.size)

        val old = c1list.first()
        val new = c2list.first()

        val target = Comparer(old,new);
        assertFalse(target.accept());
    }
}