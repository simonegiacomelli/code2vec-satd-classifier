package satd.step2

import com.github.javaparser.JavaParser
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.comments.BlockComment
import com.github.javaparser.ast.comments.Comment
import com.github.javaparser.ast.comments.JavadocComment
import com.github.javaparser.ast.comments.LineComment
import com.github.javaparser.ast.visitor.VoidVisitorAdapter

fun findMethodsByName(content: String, names: Set<String>): List<Method> {

    val cu = JavaParser().parse(content)!!

    //look for methods that matches requested names
    val foundMethods = cu.result.get().types
        .filterNotNull()
        .flatMap { type ->
            type.methods
                .filter { names.contains(it.nameAsString) }
                .map { MethodWithoutSatd(it) }
        }

    val result = mutableListOf<Method>()
    result.addAll(foundMethods)

    //add all missing names that were not found
    names.subtract(result.map { it.name })
        .forEach {
            result.add(MethodMissing(it))
        }
    return result.toList()
}


fun findMethodsWithSatd(content: String): List<Method> {
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

abstract class Method {
    abstract val name: String
    abstract val hasSatd: Boolean
    abstract val comment: String
    open val exists = true
    val childs by lazy { mutableSetOf<Method>() }
    val parents by lazy { mutableSetOf<Method>() }
    abstract val method: MethodDeclaration

    override fun equals(other: Any?): Boolean {
        TODO()
    }

    override fun hashCode(): Int {
        TODO()
    }
}


class MethodWithoutSatd(override val method: MethodDeclaration) : Method() {
    override val hasSatd get() = false
    override val name: String get() = method.nameAsString
    override val comment = ""
}

class MethodMissing(override val name: String) : Method() {
    override val hasSatd get() = false
    override val comment = ""
    override val exists = false
    override val method get() = throw IllegalStateException("This does not have a method")
}

private class MethodWithSatd(override val method: MethodDeclaration, override val comment: String) : Method() {
    override val hasSatd get() = true

    override val name get() = method.name.asString()!!

    companion object {
        fun foundIn(comment: String): Boolean {
            for (p in patterns)
                if (comment.contains(p, ignoreCase = true)) return true
            return false
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
