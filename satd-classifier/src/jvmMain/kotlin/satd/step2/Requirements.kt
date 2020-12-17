package satd.step2

import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.StringLiteralExpr
import java.io.File

class Requirements(om: Method, nm: Method) {

    val oldClean by lazy { cloneAndClean(om) }
    val newClean by lazy { cloneAndClean(nm) }

    private fun cloneAndClean(om1: Method): MethodDeclaration {
        //clone because we want the original untouched to be saved for inspection
        val clone = om1.method.clone()

        //remove all comments
        clone.setComment(null)
        clone.allContainedComments.forEach { it.remove() }

        //all non null String literals are set
        clone.findAll(StringLiteralExpr::class.java)
            .forEach {
                if (it.asString() != null && it.asString().isNotEmpty())
                    it.setString("--##string##--")
            }

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