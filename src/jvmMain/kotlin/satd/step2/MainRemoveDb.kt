package satd.step2

import satd.utils.Folders
import satd.utils.logln
import satd.utils.loglnStart

fun main() {
    loglnStart("removeDb")
    logln("removing ${Folders.database_db1.toAbsolutePath()}")
    if (!Folders.database_db1.toFile().deleteRecursively())
        throw Exception("Errore removing the database ${Folders.database_db1}")
    logln("Done")
}