package satd.step2

import com.github.javaparser.ast.body.MethodDeclaration
import satd.step1.Source

class Satd(val method: MethodDeclaration) {
    companion object {
        fun foundIn(comment: String): Boolean {
            for (p in Source.patterns)
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
        return method.equals(other)
    }

    override fun hashCode(): Int {
        return method.hashCode()
    }
}