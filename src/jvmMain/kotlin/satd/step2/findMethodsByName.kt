package satd.step2

import com.github.javaparser.JavaParser
import com.github.javaparser.ast.body.MethodDeclaration

fun findMethodsByName(content: String, names: Set<String>): List<Method> {

    val cu = JavaParser().parse(content)!!

    //look for methods that matches requested names
    val foundMethods = cu.result.get().types
        .filterNotNull()
        .flatMap { type ->
            type.methods
                .filter { names.contains(it.nameAsString) }
                .map { MethodWithoutSatd(it) }
        }

    val result = mutableListOf<Method>()
    result.addAll(foundMethods)

    //add all missing names that were not found
    names.subtract(result.map { it.name })
        .forEach {
            result.add(MethodMissing(it))
        }
    return result.toList()
}

class MethodMissing(override val name: String) : Method() {
    override val hasSatd get() = false
    override val comment = ""
    override val exists = false
    override val method get() = throw IllegalStateException("This does not have a method")
}

class MethodWithoutSatd(override val method: MethodDeclaration) : Method() {
    override val hasSatd get() = false
    override val name: String get() = method.nameAsString
    override val comment = ""
}