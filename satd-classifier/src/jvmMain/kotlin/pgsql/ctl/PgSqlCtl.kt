package pgsql.ctl

import core.ProcessStreamGlobber
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import pgsql.DsPostgreSqlProvider
import pgsql.PgDefaults
import satd.step2.assert2
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/* Simone 08/07/2014 09:49 */


class PgSqlCtl(
    private val pgsqlBinFolder: String = PgDefaults.binFolder,
    private val pgsqlDataFolder: String = PgDefaults.dataFolder,
    private val pgsqlTcpPort: Int = PgDefaults.tcpPort,
    private val pgsqlUsername: String = DsPostgreSqlProvider.USERNAME,
    private val pgsqlPassword: String = DsPostgreSqlProvider.PASSWORD,
    private val pgSqlConfigFix: IPgSqlConfigFix = PgSqlConfigFix()
) : IPgSqlCtl {
    companion object {
        private val log = LoggerFactory.getLogger(PgSqlCtl::class.java)
    }

    override fun status(): CtlStatus {
        val returnCode = exec(getPgBin("pg_ctl"), "status", "-D", dataPathStr)
        val status: CtlStatus
        status = when (returnCode) {
            0 -> CtlStatus.RUNNING
            3 -> CtlStatus.NO_SERVER_RUNNING
            else -> CtlStatus.UNKNOWN
        }
        log.info("Status: {}({})", status.toString(), returnCode)
        return status
    }

    private val dataPathStr: String get() = dataPath.toString()

    private val dataPath: Path
        get() = Paths.get(pgsqlDataFolder).toAbsolutePath().normalize()

    private fun getPgBin(exe: String): String {
        val home: Path = Paths.get(pgsqlBinFolder).toAbsolutePath().normalize()
        assert2(Files.exists(home), "Postgres home directory not found: $home")
        val bin = home.resolve("bin").resolve(exe).normalize()
        assert2(Files.exists(bin), "Not found $bin")
        return bin.toString()
    }

    override fun stopFast(): StopStatus {
        return stopInternal(true)
    }

    override fun stop(): StopStatus {
        return stopInternal(false)
    }

    fun stopInternal(fast: Boolean): StopStatus {
        val cmds = mutableListOf(getPgBin("pg_ctl"), "stop", "-t", "20", "-D", dataPathStr)
        if (fast) {
            cmds.add("-m")
            cmds.add("fast")
        }
        val returnCode = exec(*cmds.toTypedArray())
        val stopStatus = if (returnCode == 0) StopStatus.STOP_OK else StopStatus.STOP_FAILED
        log.info("Stop result: {}({})", stopStatus.toString(), returnCode)
        return stopStatus
    }

    override fun start() {
        val exitCode = exec(getPgBin("pg_ctl"), "start", "-w", "-t", "300", "-D", dataPathStr)
        log.info(
            "Start: {}",
            if (exitCode == 0) "SUCCESSFUL" else "FAILED(exitCode:$exitCode)"
        )
        if (exitCode != 0) throw PgCtlStartFailed(exitCode)
    }

    private fun exec(vararg commandTokens: String): Int {
        val tokens = listOf(*commandTokens)
        val command = ProcessBuilder().command(tokens)
        command.environment()["LANGUAGE"] = "EN"
        log.info("Running command: {}", java.lang.String.join(" ", command.command()))
        log.info("in {}", command.directory())
        log.info("with environment [{}]", command.environment()
            .map { "${it.key}=${it.value}" }.joinToString(", ").replace("\n", "\\n")
        )
        val process = command.start()
        val globber = ProcessStreamGlobber(process, processName = File(tokens[0]).name)
        globber.startGlobber()
        val returnVal = process.waitFor()
        log.info("returned {}", returnVal)
        return returnVal
    }

    override fun initDb() {
        initDb(pgsqlTcpPort)
    }

    override fun dbExist(): Boolean {
        return dataPath.toFile().exists()
    }


    fun initDb(tcpPort: Int) {
        if (dbExist()) throw PgdataAlreadyExists(dataPathStr)
        val pwFile = pwFile
        val exitValue: Int
        exitValue = try {
            exec(
                getPgBin("initdb"), "--no-locale", "-E", "UTF8", "-U", pgsqlUsername
                , "-A", "md5", "-D", dataPathStr, "--pwfile=" + pwFile.absolutePath
            )
        } finally {
            FileUtils.deleteQuietly(pwFile)
        }
        if (exitValue != 0) throw RuntimeException("Could not initialize database, initdb returned $exitValue")
        pgSqlConfigFix.fixConfig(dataPath, tcpPort)
    }


    private val pwFile: File
        get() {
            val pwFile = File.createTempFile("tmp", "tmp")
            core.FileUtils.write(pwFile, pgsqlPassword)
            return pwFile
        }

    private inner class PgdataAlreadyExists(message: String?) : RuntimeException(message)
    private inner class PgCtlStartFailed(returnCode: Int) :
        RuntimeException("return code: $returnCode")

    fun pg_dump(databaseName: String, path: String) {
        val exitValue = try {
            val tokens = listOf(
                getPgBin("pg_dump"), "-h", "localhost", "-p", "$pgsqlTcpPort", "-U", pgsqlUsername
                , "-F", "d", "-b", "-c", "-W", "-f", path, databaseName
            )

            val command = ProcessBuilder().command(tokens)
            command.environment()["LANGUAGE"] = "EN"
            log.info("Running command: {}", java.lang.String.join(" ", command.command()))
            val process = command.start()

            fun st(os: InputStream, descr: String) {
                Thread {
                    val buf = StringBuilder()
                    while (true) {
                        val ch = os.read()
                        if (ch == -1)
                            return@Thread
                        val toChar = ch.toChar()

                        buf.append(toChar)
                        if (buf.toString() == "Password: ")
                            process.outputStream
                                .bufferedWriter()
                                .also {
                                    it.write(pgsqlPassword + "\n")
                                }.flush()


                    }
                }.apply {
                    isDaemon = true
                    name = "pg_dump-$descr"
                }.start()
            }

            st(process.inputStream, "OUT")
            st(process.errorStream, "ERR")

            val returnVal = process.waitFor()
            log.info("returned {}", returnVal)
            returnVal
        } finally {

        }
        if (exitValue != 0) throw RuntimeException("Operation failed, pg_dump returned $exitValue")
        else log.info("Backup successful")

    }

    fun pg_restore(databaseName: String, path: String) {
        val exitValue = try {
            val tokens = listOf(
                getPgBin("pg_restore"), "-h", "localhost", "-p", "$pgsqlTcpPort", "-U", pgsqlUsername
                , "-W", "-c", "-d", databaseName, path
            )

            val command = ProcessBuilder().command(tokens)
            command.environment()["LANGUAGE"] = "EN"
            log.info("Running command: {}", java.lang.String.join(" ", command.command()))
            val process = command.start()

            fun st(os: InputStream, descr: String, interceptPw: Boolean) {
                Thread {
                    val buf = StringBuilder()
                    if (interceptPw)
                        while (true) {
                            val ch = os.read()
                            if (ch == -1)
                                return@Thread
                            val toChar = ch.toChar()

                            buf.append(toChar)

                            if (buf.toString() == "Password: ") {
                                process.outputStream
                                    .bufferedWriter()
                                    .also {
                                        it.write(pgsqlPassword + "\n")
                                    }.flush()
                                break
                            }

                        }
                    ProcessStreamGlobber.handleIS(os, descr)
                }.apply {
                    isDaemon = true
                    name = "pg_restore-$descr"
                }.start()
            }

            st(process.errorStream, "ERR", interceptPw = true)
            st(process.inputStream, "OUT", interceptPw = false)

            val returnVal = process.waitFor()
            log.info("returned {}", returnVal)
            returnVal
        } finally {

        }
        if (exitValue != 0) throw RuntimeException("Operation failed, pg_restore returned $exitValue")
        else log.info("Restore successful")

    }
}