package satd.step1

import com.github.javaparser.JavaParser
import com.github.javaparser.ast.comments.Comment
import java.nio.file.Path

class Source(val path: Path) {
    val satd = mutableListOf<Comment>()

    companion object {
        val patterns by lazy {
            val content = this::class.java.classLoader.getResource("satd/step1/hack-patterns.txt")!!.readText()
            content.split('\n')
                .map { it.trim() }
        }
    }

    fun filterSatd() {
        val compileUnit = JavaParser().parse(path)!!
        for (comment in compileUnit.commentsCollection.get().comments) {
            for (p in patterns)
                if (comment!!.content.contains(p, ignoreCase = true)) {
                    satd.add(comment)
                    break
                }
        }
    }

}
