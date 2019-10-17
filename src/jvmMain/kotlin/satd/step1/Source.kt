package satd.step1

import com.github.javaparser.JavaParser
import com.github.javaparser.ast.comments.Comment
import java.lang.Integer.max
import java.nio.file.Path

class Source(val path: Path, repo: Repo) {
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

    fun satdSnippets() = sequence {
        val lines = path.toFile().readLines()
        satd.forEach {
            val dup = lines.toMutableList()
            val satdLine = it.range.get().begin.line
            dup[satdLine - 1] += " <------===== DETECTED SATD"
            val begin = max(satdLine - 31, 0)
            val end = it.range.get().end.line
            val selectedLines = dup.drop(begin).take(end - begin + 60)
            val content = "-".repeat(5) + " line $begin" + "-".repeat(5) + "\n" + selectedLines.joinToString("\n")
            yield(content)
        }
    }

}
