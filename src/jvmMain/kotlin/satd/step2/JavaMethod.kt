package satd.step2

import com.github.javaparser.JavaParser
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.body.VariableDeclarator
import com.github.javaparser.ast.expr.VariableDeclarationExpr

class JavaMethod(val content: String) {
    private val res by lazy {
        JavaParser()
            .parse(wrapMethod(content))!!
            .result
            .also { assert(it.isPresent) }
            .get()
    }

    val tokenCount by lazy { res.stream().count().toInt() }
    private val methods by lazy {
        res.types
            .filterNotNull()
            .flatMap { type ->
                type.methods
                    .filter { it.body.isPresent }
            }
    }
    val valid: Boolean by lazy {
        methods.count() == 1
                && !hasInnerMethods
                && !methods.first().body.isEmpty
                && accept(methods.first())


    }

    private fun accept(m: MethodDeclaration): Boolean {
        if (m.isDefault) return false
        val body = m.body.get()
        if (body.statements.isEmpty())
            return false
        if (body.statements.size == 1 && body.statements[0].isThrowStmt)
            return false
        //enum
        if (body.findAll(VariableDeclarator::class.java).any {
                it.name.asString() == "enum"
            }) return false

        return true
    }

    private val hasInnerMethods: Boolean by lazy {
        try {
            methods
                .any {
                    it.body.get().findFirst(MethodDeclaration::class.java).isPresent
                }

        } catch (ex: Exception) {
            println("error in [[$content]]")
            throw ex
        }
    }

}