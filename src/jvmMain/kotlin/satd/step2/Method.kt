package satd.step2

import com.github.javaparser.ast.body.MethodDeclaration

abstract class IMethod {
    abstract val name: String
    abstract val hasSatd: Boolean
    val childs by lazy { mutableSetOf<IMethod>() }
    val parents by lazy { mutableSetOf<IMethod>() }

}

class Method(val method: MethodDeclaration, val comment: String) : IMethod() {
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

    override fun equals(obj: Any?): Boolean {
        return if (obj == null || obj !is Method) {
            false
        } else method.equals(obj.method)
    }

    override fun hashCode(): Int {
        return method.hashCode()
    }
}