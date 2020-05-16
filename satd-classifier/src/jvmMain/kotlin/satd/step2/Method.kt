package satd.step2

import com.github.javaparser.ast.body.MethodDeclaration


abstract class Method {
    abstract val name: String
    abstract val hasSatd: Boolean
    abstract val comment: String
    open val exists = true
    abstract val method: MethodDeclaration
    open val pattern: String = ""
}


