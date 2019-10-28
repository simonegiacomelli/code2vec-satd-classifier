package satd.step2

class Satd{
    companion object {
        val patterns by lazy {
            val content = this::class.java.classLoader.getResource("satd/step1/hack-patterns.txt")!!.readText()
            content.split('\n')
                .map { it.trim() }
        }
    }
}