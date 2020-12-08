package pgsql

/* User: Simone 30/03/13 9.32 */

object DsPostgreSqlProvider {
    const val HOST = "localhost"
    const val NAME = "db"
    const val PORT = 1603
    val USERNAME: String by lazy { System.getenv("USER") ?: "postgres" }
    const val PASSWORD = "usi"
}
