package satd.step2

import kotlin.test.Test
import kotlin.test.assertEquals

internal class ComparerTest{
    val class1 by lazy { load("Class1.java") }
    val class2 by lazy { load("Class2.java") }

    private fun load(s: String) = this::class.java.classLoader.getResource("satd/step2/ComparerTest/$s")!!.readText()

    @Test
    fun `x`() {
    }
}