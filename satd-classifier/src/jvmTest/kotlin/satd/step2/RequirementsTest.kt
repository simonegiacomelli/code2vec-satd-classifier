package satd.step2

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RequirementsTest {
    val class1 by lazy { load("satd/step2/ComparerTest/Class1.java") }
    val class2 by lazy { load("satd/step2/ComparerTest/Class2.java") }

    val case3old by lazy { load("satd/step2/ComparerTest/Case3_old.java") }
    val case3new by lazy { load("satd/step2/ComparerTest/Case3_new.java") }

    val stringCaseOld by lazy { load("satd/step2/ComparerTest/StringCaseOld.java") }
    val stringCaseNew by lazy { load("satd/step2/ComparerTest/StringCaseNew.java") }


    private fun load(s: String) = this::class.java.classLoader.getResource("$s")!!.readText()

    @Test
    fun `no changes in the code should reject the satd`() {
        val target = instantiateTarget(class1, class2, "method1")
        assertFalse(target.accept());
    }

    @Test
    fun `correct satd that disappear with code change should be accepted`() {
        val target = instantiateTarget(case3old, case3new, "method1")
        assertTrue(target.accept());
    }

    @Test
    fun `a method that changes only betweeen non empty strings should be rejected`() {
        val target = instantiateTarget(stringCaseOld, stringCaseNew, "method1")
        assertFalse(target.accept());
    }

    @Test
    fun `a method that change from empty string to non empty string instances should be accepted`() {
        val target = instantiateTarget(stringCase2Old, stringCase2New, "method1")
        assertTrue(target.accept());
    }

    @Test
    fun `a method that change from empty string to null string instances should be accepted`() {
        val target = instantiateTarget(stringCase3Old, stringCase3New, "method1")
        assertTrue(target.accept());
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


val stringCase2Old = """
    package satd.step2;

    class Class1 {
        void method1(int code) {
            //fixme - test case from emtpy string to null
            if (cod > 20)
                System.out.println(String.format("", code));
            for (int i = 0; i < 20; i++)
                method2();
        }

        double method2() {
            int offset = 10;
            return java.lang.Math.random() + offset;
        }

    }
""".trimIndent()

val stringCase2New = """
    package satd.step2;

    class Class1 {
        void method1(int code) {
            if (cod > 20)
                System.out.println(String.format("hello", code));
            for(int i = 0; i<20;i++)
                method2();
        }

        double method2() {
            int offset = 10;
            return java.lang.Math.random() + offset;
        }

    }
""".trimIndent()

val stringCase3Old = """
    package satd.step2;

    class Class1 {
        void method1(int code) {
            // fixme from empty string to null
            if (cod > 20)
                System.out.println(String.format("", code));
            for(int i = 0; i<20;i++)
                method2();
        }

        double method2() {
            int offset = 10;
            return java.lang.Math.random() + offset;
        }

    }
""".trimIndent()

val stringCase3New = """
    package satd.step2;

    class Class1 {
        void method1(int code) {
            if (cod > 20)
                System.out.println(String.format(null, code));
            for(int i = 0; i<20;i++)
                method2();
        }

        double method2() {
            int offset = 10;
            return java.lang.Math.random() + offset;
        }

    }
""".trimIndent()

