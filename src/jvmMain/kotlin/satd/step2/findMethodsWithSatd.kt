package satd.step2

import com.github.javaparser.JavaParser
import com.github.javaparser.ast.body.MethodDeclaration
import java.util.regex.Pattern

private const val satdToIgnore = "there is a problem"

fun findMethodsWithSatd(content: String): List<Method> {

    val satdList = mutableSetOf<MethodWithSatd>()
    val methodList = mutableSetOf<MethodDeclaration>()

    /* this collection of MethodDeclaration should not be needed. we should rely only on the previous collection */
    fun addMethod(method: MethodDeclaration, comment: String): Boolean {

        val pattern = MethodWithSatd.match(comment)
        if (pattern != null) {
            if (!methodList.contains(method)) {
                satdList.add(MethodWithSatd(method, comment, pattern))
                methodList.add(method)
            }
        }
        return pattern != null
    }

    val cu = JavaParser().parse(content)!!
    if (!cu.result.isPresent)
        return emptyList()

    cu.result.get().types.filterNotNull().forEach { type ->

        type.methods.forEach { method ->
            val g = method.allContainedComments.filterNotNull()
                .map { it.content.orEmpty().trim() }
                .map { it.split(" ").filter { it.trim() != "*" }.map { it.trim() }.joinToString(" ") }
                .joinToString(" ")

            addMethod(method, g)
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
            for ((p, r) in reg)
                if (comment.contains(p, ignoreCase = true) &&
                    r.matcher(comment).find()
                )
                    return p
            return null
        }

        val patterns by lazy {
            val content = this::class.java.classLoader.getResource("satd/step2/hack-patterns.txt")!!.readText()
            content.split('\n')
                .map { it.trim() }
                .filter { !it.startsWith("#") }
        }
        val reg by lazy { patterns.map { Pair(it, Pattern.compile("""\b$it\b""", Pattern.CASE_INSENSITIVE)!!) } }
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