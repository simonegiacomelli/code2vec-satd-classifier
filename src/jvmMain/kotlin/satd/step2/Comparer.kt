package satd.step2

import com.github.javaparser.ast.body.MethodDeclaration

class Comparer(om: Method, nm: Method) {

    val old by lazy { om.method.clone() }
    val new by lazy { nm.method.clone() }

    fun accept(): Boolean {

        removeComments(old)
        removeComments(new)

        return old != new
    }

    private fun removeComments(m: MethodDeclaration) {
        m.allContainedComments.forEach { it.remove() }
    }


}