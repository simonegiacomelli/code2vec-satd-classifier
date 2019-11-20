package satd.step2

import com.github.javaparser.ast.body.MethodDeclaration
import java.io.File

class Requirements(om: Method, nm: Method) {

    val oldClean by lazy { cloneAndClean(om) }
    val newClean by lazy { cloneAndClean(nm) }

    private fun cloneAndClean(om1: Method): MethodDeclaration {
        val clone = om1.method.clone()
        clone.setComment(null)
        clone.allContainedComments.forEach { it.remove() }
        return clone
    }

    fun accept(): Boolean = oldClean != newClean

    private fun debug() {
        writeToFile(oldClean, "compare/old.java")
        writeToFile(newClean, "compare/new.java")
    }

    private fun writeToFile(m: MethodDeclaration, path: String) {
        File(path).apply {
            parentFile.mkdirs()
            writeText("$m")
        }
    }

}