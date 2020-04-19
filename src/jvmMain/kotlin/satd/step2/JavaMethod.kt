package satd.step2

import com.github.javaparser.JavaParser
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.stmt.BlockStmt

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
                && accept(methods.first().body.get())


    }

    private fun accept(get: BlockStmt): Boolean {
        if(get.statements.isEmpty())
            return false
        if(get.statements.size == 1 && get.statements[0].isThrowStmt)
            return false
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