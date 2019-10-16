package satd.step1

import com.github.javaparser.JavaParser
import java.io.StringReader



class RepoInspector

{

    companion object{
        @JvmStatic
        fun main(args: Array<String>) {
            val classText = """public class Dummy{
//Comment
}
//second comment
"""
            val reader = StringReader(classText)
            val compilationUnit = JavaParser().parse(reader)!!

            for (comment in compilationUnit.commentsCollection.get().comments) {
                logln(comment.getContent())
            }
        }
    }
}