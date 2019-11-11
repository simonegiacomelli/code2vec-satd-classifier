package satd.step2

import com.github.javaparser.JavaParser
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.comments.BlockComment
import com.github.javaparser.ast.comments.Comment
import com.github.javaparser.ast.comments.JavadocComment
import com.github.javaparser.ast.comments.LineComment
import com.github.javaparser.ast.visitor.VoidVisitorAdapter

fun findMethodsWithSatd(content: String): List<Method> {
    val satdList = mutableSetOf<MethodWithSatd>()
    val methodList = mutableSetOf<MethodDeclaration>()
    /* this collection of MethodDeclaration should not be needed. we should rely only on the previous collection */
    fun addMethod(method: MethodDeclaration, comment: Comment) {

        val pattern = MethodWithSatd.match(comment.content)
        if (pattern != null)
            if (!methodList.contains(method)) {
                satdList.add(MethodWithSatd(method, comment.content, pattern))
                methodList.add(method)
            }
    }

    val cu = JavaParser().parse(content)!!
    cu.result.get().types.filterNotNull().forEach { type ->
        type.methods.forEach { method ->
            method.accept(object : VoidVisitorAdapter<Node>() {
                override fun visit(n: BlockComment, arg: Node?) {
                    addMethod(method, n)
                }

                override fun visit(n: LineComment, arg: Node?) {
                    addMethod(method, n)
                }

                override fun visit(n: JavadocComment, arg: Node?) {
                    addMethod(method, n)
                }

            }, null)
        }
    }
    return satdList.toList()
}

class MethodWithSatd(
    override val method: MethodDeclaration,
    override val comment: String,
    override val pattern: String
) : Method() {

    override val hasSatd get() = true

    override val name get() = method.name.asString()!!

    companion object {
        fun match(comment: String): String? {
            for (p in patterns)
                if (comment.contains(p, ignoreCase = true)) return p
            return null
        }

        val patterns by lazy {
            val content = this::class.java.classLoader.getResource("satd/step1/hack-patterns.txt")!!.readText()
            content.split('\n')
                .map { it.trim() }
        }
    }

    override fun equals(other: Any?): Boolean {
        return if (other == null || other !is MethodWithSatd) {
            false
        } else method.equals(other.method)
    }

    override fun hashCode(): Int {
        return method.hashCode()
    }
}