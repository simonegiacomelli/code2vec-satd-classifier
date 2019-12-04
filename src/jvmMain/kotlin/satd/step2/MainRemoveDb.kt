package satd.step2

import satd.utils.Folders
import satd.utils.logln

fun main() {
    logln("removing ${Folders.database_db1.toAbsolutePath()}")
    if (!Folders.database_db1.toFile().deleteRecursively())
        throw IllegalStateException("Errore removing the database ${Folders.database_db1}")
    logln("Done")
}