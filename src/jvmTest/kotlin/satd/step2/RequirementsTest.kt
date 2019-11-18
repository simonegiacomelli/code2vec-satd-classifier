package satd.step2

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

internal class RequirementsTest {
    val class1 by lazy { load("Class1.java") }
    val class2 by lazy { load("Class2.java") }

    private fun load(s: String) = this::class.java.classLoader.getResource("satd/step2/ComparerTest/$s")!!.readText()

    @Test
    fun `no changes in the code should reject the satd`() {
        val c1list = findMethodsWithSatd(class1)
        val c2list = findMethodsByName(class2, setOf("method1"))
        assertEquals(1, c1list.size)
        assertEquals(1, c2list.size)

        val old = c1list.first()
        val new = c2list.first()

        val target = Requirements(old,new);
        assertFalse(target.accept());
    }
}