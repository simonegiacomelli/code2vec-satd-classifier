package satd.step1

import java.nio.file.Paths

class Folders {
    companion object {
        val data get() = Paths.get("./data")
        val repos get() = data.resolve("repos")
        val satd get() = data.resolve("satd")
        val database get() = data.resolve("database")
        val database_db1 get() = database.resolve("db1").toAbsolutePath().normalize()!!
        val guineaPigRepos get() = data.resolve("guinea_pig_repos")
    }
}