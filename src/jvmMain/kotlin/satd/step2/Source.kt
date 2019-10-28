package satd.step2

import com.github.javaparser.JavaParser

class Source(val content: String) {
    val satdList = mutableListOf<Satd>()

    init {
        val compileUnit = JavaParser().parse(content)!!
        for (comment in compileUnit.commentsCollection.get().comments) {
            for (p in Satd.patterns)
                if (comment!!.content.contains(p, ignoreCase = true)) {
                    satdList.add(Satd())
                }
        }
    }
}