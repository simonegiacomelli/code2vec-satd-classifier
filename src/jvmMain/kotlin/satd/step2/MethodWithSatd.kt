package satd.step2

import com.github.javaparser.ast.body.MethodDeclaration

abstract class IMethod {
    abstract val name: String
    abstract val hasSatd: Boolean
    abstract val comment: String
    open val exists = true
    val childs by lazy { mutableSetOf<IMethod>() }
    val parents by lazy { mutableSetOf<IMethod>() }

    override fun equals(other: Any?): Boolean {
        TODO()
    }

    override fun hashCode(): Int {
        TODO()
    }
}

class MethodWithSatd(val method: MethodDeclaration, override val comment: String) : IMethod() {
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