package satd.step2

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

internal class RequirementsTest {
    val class1 by lazy { load("Class1.java") }
    val class2 by lazy { load("Class2.java") }
    val a1old by lazy { load("Android1_old.java") }
    val a1new by lazy { load("Android1_new.java") }

    private fun load(s: String) = this::class.java.classLoader.getResource("satd/step2/ComparerTest/$s")!!.readText()

    @Test
    fun `no changes in the code should reject the satd`() {
        verify(class1, class2, "method1")
    }

    @Test
    fun `no changes in the code should reject the satd case from repo doshivikram_jchat4android`() {
        verify(a1old, a1new, "onReceive")
    }

    private fun verify(cold: String, cnew: String, name: String) {
        val c1list = findMethodsWithSatd(cold)
        val c2list = findMethodsByName(cnew, setOf(name))
        assertEquals(1, c1list.size)
        assertEquals(1, c2list.size)

        val old = c1list.first()
        val new = c2list.first()

        val target = Requirements(old, new);
        assertFalse(target.accept());
    }
}