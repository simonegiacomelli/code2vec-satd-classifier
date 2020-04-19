package satd.step2

import com.github.javaparser.JavaParser
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.MethodDeclaration

class JavaMethod(val content: String) {
    private val res by lazy {
        JavaParser()
            .parse(wrapMethod(content))!!
            .result
            .also { assert(it.isPresent) }
            .get()
    }

    val tokenCount by lazy { res.stream().count().toInt() }

    val hasInnerMethods: Boolean by lazy {
        try {


            res.types
                .filterNotNull()
                .any { type ->
                    type.methods
                        .filter { it.body.isPresent }
                        .any {
                        it.body.get().findFirst(MethodDeclaration::class.java).isPresent
                    }
                }
        } catch (ex: Exception) {
            println("error in [[$content]]")
            throw ex
        }
    }

}