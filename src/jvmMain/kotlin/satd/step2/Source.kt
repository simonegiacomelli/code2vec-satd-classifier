package satd.step2

import com.github.javaparser.JavaParser
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.comments.BlockComment
import com.github.javaparser.ast.comments.Comment
import com.github.javaparser.ast.comments.JavadocComment
import com.github.javaparser.ast.comments.LineComment
import com.github.javaparser.ast.visitor.VoidVisitorAdapter


class Source(content: String) {
    val satdMethods = compileSatd(content)

    private fun compileSatd(content: String): List<Method> {
        val satdList = mutableSetOf<Method>()
        val methodList = mutableSetOf<MethodDeclaration>()
        /* this collection of MethodDeclaration should not be needed. we should rely only on the previous collection */
        fun addMethod(method: MethodDeclaration, comment: Comment) {

            if (Method.foundIn(comment.content))
                if (!methodList.contains(method)) {
                    satdList.add(Method(method,comment.content))
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
}