package satd.step2


import kotlin.test.Test
import kotlin.test.assertEquals


internal class FindMethodWithSatdTest {
    val class1 by lazy { load("Class1.java") }
    val class2 by lazy { load("Class2.java") }
    val class2b by lazy { load("Class2b.java") }
    val class3 by lazy { load("Class3.java") }
    val class4 by lazy { load("Class4.java") }
    val class5block by lazy { load("Class5block.java") }
    val class5jdoc by lazy { load("Class5jdoc.java") }
    val class5line by lazy { load("Class5line.java") }
    val fixmethod by lazy { load("Fixmethod.java") }
    val throwsInJavaDoc by lazy { load("SatdInJavaDocThrows.java") }

    private fun load(s: String) = this::class.java.classLoader.getResource("satd/step2/SourceTest/$s")!!.readText()

    @Test
    fun `method with satd should be correctly detected`() {
        val target = findMethodsWithSatd(class1)
        assertEquals(1, target.size)
    }

    @Test
    fun `normal comment should be ignored`() {
        val target = findMethodsWithSatd(class2)
        assertEquals(0, target.size)
    }

    @Test
    fun `normal comment should be ignored b`() {
        val target = findMethodsWithSatd(class2b)
        assertEquals(0, target.size)
    }

    @Test
    fun `bait comment on field should be ignored`() {
        val target = findMethodsWithSatd(class3)
        assertEquals(0, target.size)
    }

    @Test
    fun `double satd comment in one method should be accounted as one`() {
        val target = findMethodsWithSatd(class4)
        assertEquals(1, target.size)
    }

    @Test
    fun `method block comment`() {
        val target = findMethodsWithSatd(class5block)
        assertEquals(1, target.size)
    }

    @Test
    fun `method jdoc comment`() {
        val target = findMethodsWithSatd(class5jdoc)
        assertEquals(1, target.size)
    }

    @Test
    fun `comment fixmethod should not be matched`() {
        val target = findMethodsWithSatd(fixmethod)
        assertEquals(0, target.size)
    }

    @Test
    fun `some patterns must not be detected if they are in javadoc throws clause`() {
        val target = findMethodsWithSatd(throwsInJavaDoc)
        assertEquals(0, target.size)
    }

    //todo should I include this?
    //@Test
    fun `method line comment`() {
        val target = findMethodsWithSatd(class5line)
        assertEquals(1, target.size)
    }

    //TODO detect failed parsing of incorrect java source
    //TODO add method name correctenss check
}