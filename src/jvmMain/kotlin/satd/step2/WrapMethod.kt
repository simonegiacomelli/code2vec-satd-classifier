package satd.step2

fun wrapMethod(methodSource: String): String {
    val content =
        """
public class Wrapper{
${methodSource.prependIndent()}
}
""".trimIndent()
    return content
}
