package satd.step2

import com.github.javaparser.ast.body.MethodDeclaration

class Method(val method: MethodDeclaration, val comment: String) {
    val childs by lazy { mutableSetOf<Method>() }
    val parents by lazy { mutableSetOf<Method>() }



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