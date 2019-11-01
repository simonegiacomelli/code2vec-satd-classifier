package satd.step2

import com.github.javaparser.ast.body.MethodDeclaration

class Satd(val method: MethodDeclaration) {
    val childs by lazy { mutableSetOf<Satd>() }
    val parents by lazy { mutableSetOf<Satd>() }

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
        return if (obj == null || obj !is Satd) {
            false
        } else method.equals(obj.method)
    }

    override fun hashCode(): Int {
        return method.hashCode()
    }
}