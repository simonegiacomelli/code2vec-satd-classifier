package satd.step2


import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


internal class FindMethodWithSatdTest {
    val class1 by lazy { load("Class1.java") }
    val class2 by lazy { load("Class2.java") }
    val class2b by lazy { load("Class2b.java") }
    val class3 by lazy { load("Class3.java") }
    val class4 by lazy { load("Class4.java") }
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
    fun `comment fixmethod should not be matched`() {
        val target = findMethodsWithSatd(fixmethod)
        assertEquals(0, target.size)
    }

    @Test
    fun `some patterns must not be detected if they are in javadoc throws clause`() {
        val target = findMethodsWithSatd(throwsInJavaDoc)
        assertEquals(0, target.size)
    }

    @Test
    fun `no satd detection in JavaDoc`() {
        val target = findMethodsWithSatd(noSatdDetectionInJavaDoc)
        assertEquals(0, target.size)
    }

    @Test
    fun `no satd detection in method comment`() {
        val target = findMethodsWithSatd(noSatdDetectionInMethodComment)
        assertEquals(0, target.size)
    }

    @Test
    fun `a satd split on multiple line block comments should be detected`() {
        val target = findMethodsWithSatd(satdWithCarriageReturn)
        assertEquals(1, target.size)
    }

    @Test
    fun `a satd split on in a block comment with multiple lines should be detected`() {
        val target = findMethodsWithSatd(satdWithMultiLineBlockComment)
        assertEquals(1, target.size)
    }

    //todo find a way to easily implement this
    //@Test
    fun `a satd split by statements should NOT be detected`() {
        val target = findMethodsWithSatd(noRealSatd)
        assertEquals(1, target.size)
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


val noSatdDetectionInJavaDoc = """
    class Class1 {
        /**
         * some comment fixme, it is hacky and ugly
         *
         * @throws IOException If there is a problem opening the file representing
         *                     the bookmark.
         */
        public void loadAutoBookmark() throws IOException {
            autoBookmark = Bookmark.getInstance(book.getPath());
            audioOffset = autoBookmark.getPosition();
            book.setCurrentIndex(autoBookmark.getNccIndex());
            book.goTo(book.current());
        }
    }
    
""".trimIndent()
val noSatdDetectionInMethodComment = """
    class Class1 {
        /*
         * some comment fixme, it is hacky and ugly
         *
         */
        public void loadAutoBookmark() throws IOException {
            autoBookmark = Bookmark.getInstance(book.getPath());
            audioOffset = autoBookmark.getPosition();
            book.setCurrentIndex(autoBookmark.getNccIndex());
            book.goTo(book.current());
        }
    }
""".trimIndent()


val satdWithCarriageReturn = """
    package satd.step2;

    class Class1 {
        void method1(int code) {
            // this comments contains 2 satd
            // this indicates a more 
            // fundamental problem temporary 
            // crutch
            if (cod > 20)
                System.out.println(String.format("", code));
            for(int i = 0; i<20;i++)
                Class2.method2();
        }
    }
""".trimIndent()

val satdWithMultiLineBlockComment = """
    package satd.step2;

    class Class1 {
        void method1(int code) {
            /* this comments contains 2 satd
             this indicates a more 
             fundamental problem temporary 
             crutch
             */
            if (cod > 20)
                System.out.println(String.format("", code));
            for(int i = 0; i<20;i++)
                Class2.method2();
        }
    }
""".trimIndent()


val noRealSatd = """
    package satd.step2;

    class Class1 {
        void method1(int code) {
            /* 1 this comments contains 2 satd
             code should considered more fundamental 
             */
            if (cod > 20)
                System.out.println(String.format("", code));
            // 2.1 problem would arise if XYZ
            // 2.2 and then ABC
            for(int i = 0; i<20;i++){
                //3.1  for loop before comment
                /* 3.2 */
                Class2.method2();
                //4.1 for loop after comment
                //4.2
            }
                
            //5 ending comment
        }
    }
""".trimIndent()
