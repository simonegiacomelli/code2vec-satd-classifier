package satd.step1

import java.nio.file.Paths

class Folders {
    companion object {
        val repos get() = Paths.get("./data/repos/")
        val satd get() = Paths.get("./data/satd/")
    }
}