package satd.step2

import com.github.javaparser.ast.comments.Comment

class Comparer(om: Method, nm: Method) {
    val old by lazy { om.method.clone() }
    val new = nm.method
    fun accept(): Boolean {

        return old != new
    }

}