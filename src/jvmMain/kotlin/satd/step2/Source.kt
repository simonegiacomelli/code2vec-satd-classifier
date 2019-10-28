package satd.step2

import com.github.javaparser.JavaParser
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.comments.BlockComment
import com.github.javaparser.ast.comments.Comment
import com.github.javaparser.ast.comments.JavadocComment
import com.github.javaparser.ast.comments.LineComment
import com.github.javaparser.ast.visitor.VoidVisitorAdapter


class Source(val content: String) {
    val satdList = mutableSetOf<Satd>()
    val methodList = mutableSetOf<MethodDeclaration>()
    fun addMethod(method: MethodDeclaration, comment: Comment) {
//        if(!satdList.contains(Satd(method)))
//            satdList.add(Satd(method))
        if (Satd.foundIn(comment.content))
            if (!methodList.contains(method)) {
                satdList.add(Satd(method))
                methodList.add(method)
            }
    }

    init {
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
    }
}