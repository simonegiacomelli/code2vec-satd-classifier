package satd.step2

import com.github.javaparser.ast.body.MethodDeclaration
import java.io.File

class Requirements(om: Method, nm: Method) {

    val old by lazy { om.method.clone() }
    val new by lazy { nm.method.clone() }

    fun accept(): Boolean {

        removeComments(old)
        removeComments(new)

        return old != new
    }

    private fun debug() {
        writeToFile(old, "compare/old.java")
        writeToFile(new, "compare/new.java")
    }

    private fun writeToFile(m: MethodDeclaration, path: String) {
        File(path).apply {
            parentFile.mkdirs()
            writeText("$m")
        }
    }

    private fun removeComments(m: MethodDeclaration) {
        m.setComment(null)
        m.allContainedComments.forEach { it.remove() }
    }


}