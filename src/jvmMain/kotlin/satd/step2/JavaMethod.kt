package satd.step2

import com.github.javaparser.JavaParser

class JavaMethod(content: String) {
    private val res by lazy { JavaParser().parse(wrapMethod(content))!! }
    val tokenCount by lazy {
        assert(res.result.isPresent)
        val cu = res.result.get()
        val x = cu.stream().count()
        x.toInt()
    }

}