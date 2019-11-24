package satd.step2

import com.github.javaparser.JavaParser
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.comments.Comment
import com.github.javaparser.ast.comments.JavadocComment
import com.github.javaparser.javadoc.JavadocBlockTag
import java.util.regex.Pattern

private val satdToIgnore = "there is a problem"

fun findMethodsWithSatd(content: String): List<Method> {

    val satdList = mutableSetOf<MethodWithSatd>()
    val methodList = mutableSetOf<MethodDeclaration>()
    fun matchJavaDoc(comment: JavadocComment): String? {
        val p1 = MethodWithSatd.match(comment.content) ?: return null
        if (p1 != satdToIgnore) return p1

        val parse = comment.parse()
        //potential false positive
        val potentialFP = parse
            .blockTags
            .filter { it.tagName == "throws" }
            .filter { it.toText().contains(satdToIgnore) }

        if (potentialFP.isEmpty())
            return p1

        //we remove the "satdToIgnore" from the javadoc and re-check the satd
        parse.blockTags.removeAll(potentialFP)
        parse.blockTags.addAll(potentialFP.map {
            JavadocBlockTag("throws", it.toText().replace(satdToIgnore, ""))
        })
        val ret = MethodWithSatd.match(parse.toText())
        return ret
    }

    fun matchSatd(comment: Comment): String? {
        val pattern = if (comment !is JavadocComment)
            MethodWithSatd.match(comment.content)
        else
            matchJavaDoc(comment)
        return pattern
    }

    /* this collection of MethodDeclaration should not be needed. we should rely only on the previous collection */
    fun addMethod(method: MethodDeclaration, comment: Comment): Boolean {

        val pattern = matchSatd(comment)
        if (pattern != null) {
            if (!methodList.contains(method)) {
                satdList.add(MethodWithSatd(method, comment.content, pattern))
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
            if (!method.comment.isPresent || !addMethod(method, method.comment.get()))
                method.allContainedComments.forEach c@{
                    if (addMethod(method, it))
                        return@c
                }


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
            val content = this::class.java.classLoader.getResource("satd/step1/hack-patterns.txt")!!.readText()
            content.split('\n')
                .map { it.trim() }
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