package satd.step1

import com.github.javaparser.JavaParser
import java.io.StringReader

class CommentFinder {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val classText = """
   /* multi line    
     comment */
                public class Dummy{
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