package satd.step2

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RequirementsTest {
    val class1 by lazy { load("Class1.java") }
    val class2 by lazy { load("Class2.java") }
    val a1old by lazy { load("Android1_old.java") }
    val a1new by lazy { load("Android1_new.java") }
    val case3old by lazy { load("Case3_old.java") }
    val case3new by lazy { load("Case3_new.java") }
    val stringCaseOld by lazy { load("StringCaseOld.java") }
    val stringCaseNew by lazy { load("StringCaseNew.java") }

    private fun load(s: String) = this::class.java.classLoader.getResource("satd/step2/ComparerTest/$s")!!.readText()

    @Test
    fun `no changes in the code should reject the satd`() {
        val target = instantiateTarget(class1, class2, "method1")
        assertFalse(target.accept());
    }

    @Test
    fun `no changes in the code should reject the satd case from repo doshivikram_jchat4android`() {
        val target = instantiateTarget(a1old, a1new, "onReceive")
        assertFalse(target.accept());
    }

    @Test
    fun `correct satd that disappear with code change should be accepted`() {
        val target = instantiateTarget(case3old, case3new, "method1")
        assertTrue(target.accept());
    }

    @Test
    fun `a method that changes only in strings should be rejected`() {
        val target = instantiateTarget(stringCaseOld, stringCaseNew, "method1")
        assertFalse(target.accept());
    }

    private fun instantiateTarget(cold: String, cnew: String, name: String): Requirements {
        val c1list = findMethodsWithSatd(cold)
        val c2list = findMethodsByName(cnew, setOf(name))
        assertEquals(1, c1list.size)
        assertEquals(1, c2list.size)

        val old = c1list.first()
        val new = c2list.first()

        return Requirements(old, new)
    }
}