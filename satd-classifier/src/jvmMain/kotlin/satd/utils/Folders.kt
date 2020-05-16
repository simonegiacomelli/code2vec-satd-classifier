package satd.utils

import java.nio.file.Paths


class FolderConf(val config: Config) {
    val data get() = Paths.get("./data")

    val repos get() = config.run { if (repos_path.isNullOrBlank()) data.resolve("repos") else Paths.get(repos_path) }

    val satd get() = data.resolve("satd")
    val log get() = data.resolve("log")
    val diff get() = data.resolve("diff")
    val dataset get() = data.resolve("dataset")
    val cache get() = data.resolve("cache")
    val database get() = data.resolve("database")
    val heapdumps get() = data.resolve("heapdumps")
    val database_db1 get() = database.resolve("db1").toAbsolutePath().normalize()!!
    val guineaPigRepos get() = data.resolve("guinea_pig_repos")

    val temp get() = data.resolve("temp")

}

val Folders = FolderConf(config)