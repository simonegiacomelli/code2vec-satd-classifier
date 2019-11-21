package satd.step1

import java.nio.file.Paths

class Folders {
    companion object {
        val data get() = Paths.get("./data")
        val repos get() = data.resolve("repos")
        val satd get() = data.resolve("satd")
        val log get() = data.resolve("log")
        val cache get() = data.resolve("cache")
        val database get() = data.resolve("database")
        val heapdumps get() = data.resolve("heapdumps")
        val database_db1 get() = database.resolve("db1").toAbsolutePath().normalize()!!
        val guineaPigRepos get() = data.resolve("guinea_pig_repos")
        val temp get() = data.resolve("temp")
    }
}