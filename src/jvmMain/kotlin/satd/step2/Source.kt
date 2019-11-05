package satd.step2

import com.github.javaparser.JavaParser
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.comments.BlockComment
import com.github.javaparser.ast.comments.Comment
import com.github.javaparser.ast.comments.JavadocComment
import com.github.javaparser.ast.comments.LineComment
import com.github.javaparser.ast.visitor.VoidVisitorAdapter

fun findMethodsWithSatd(content: String): List<MethodWithSatd> {
    val satdList = mutableSetOf<MethodWithSatd>()
    val methodList = mutableSetOf<MethodDeclaration>()
    /* this collection of MethodDeclaration should not be needed. we should rely only on the previous collection */
    fun addMethod(method: MethodDeclaration, comment: Comment) {

        if (MethodWithSatd.foundIn(comment.content))
            if (!methodList.contains(method)) {
                satdList.add(MethodWithSatd(method, comment.content))
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

class MethodWithoutSatd(override val name: String) : IMethod() {
    override val hasSatd get() = false
    override val comment = ""
}

class MethodMissing(override val name: String) : IMethod() {
    override val hasSatd get() = false
    override val comment = ""
    override val exists = false
}

fun findMethodsByName(content: String, names: Set<String>): List<IMethod> {

    val cu = JavaParser().parse(content)!!

    //look for methods that matches requested names
    val foundMethods = cu.result.get().types
        .filterNotNull()
        .flatMap { type ->
            type.methods
                .filter { names.contains(it.nameAsString) }
                .map { MethodWithoutSatd(it.nameAsString) }
        }

    val result = mutableListOf<IMethod>()
    result.addAll(foundMethods)

    //add all missing names that were not found
    names.subtract(result.map { it.name })
        .forEach {
            result.add(MethodMissing(it))
        }
    return result.toList()
}
