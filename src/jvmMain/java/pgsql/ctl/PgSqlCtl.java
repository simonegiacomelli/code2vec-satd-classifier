package pgsql.ctl;

/* Simone 08/07/2014 09:49 */

import core.FileUtils;
import core.IAppProperties;
import core.ProcessStreamGlobber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pgsql.DsPostgreSqlProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static satd.step2.MainGenDatasetKt.assert2;

public class PgSqlCtl implements IPgSqlCtl {

    public static final String DATABASE_SUBFOLDER = "pg";
    public static final int DEFAULT_TCP_PORT = 1603;

    private static final Logger log = LoggerFactory.getLogger(PgSqlCtl.class);

    private final IPgSqlConfigFix pgSqlConfigFix;
    private final IAppProperties appProperties;


    public PgSqlCtl(IPgSqlConfigFix pgSqlConfigFix, IAppProperties appProperties) {
        this.pgSqlConfigFix = pgSqlConfigFix;
        this.appProperties = appProperties;
    }

    @Override
    public CtlStatus status() {
        int returnCode = exec(getPgBin("pg_ctl"), "status", "-D", getDb());
        CtlStatus status;
        switch (returnCode) {
            case 0:
                status = CtlStatus.RUNNING;
                break;
            case 3:
                status = CtlStatus.NO_SERVER_RUNNING;
                break;
            default:
                status = CtlStatus.UNKNOWN;
        }
        log.info("Status: {}({})", status.toString(), returnCode);
        return status;
    }

    private String getDb() {
        String res = getDbPath().toString();
        return res;
    }

    private Path getDbPath() {
        return Paths.get(appProperties.getProperty("pgsql.data.folder", "data/db/")).resolve(DATABASE_SUBFOLDER).normalize();
    }

    private String getPgBin(String exe) {
        Path home;

        home = Paths.get(appProperties.getProperty("pgsql.bin.folder", "data/pgsql"))
                .toAbsolutePath().normalize();

        assert2(Files.exists(home), "Postgres home directory not found: " + home);
        Path bin = home.resolve("bin")
                .resolve(exe)
                .normalize();
        assert2(Files.exists(bin), "Not found " + bin);
        return bin.toString();
    }

    @Override
    public StopStatus stopFast() {
        return stopInternal(true);
    }

    @Override
    public StopStatus stop() {
        return stopInternal(false);
    }

    public StopStatus stopInternal(boolean fast) {
        ArrayList<String> cmds = new ArrayList<>(
                Arrays.asList(getPgBin("pg_ctl"), "stop", "-t", "20", "-D", getDb()));
        if (fast) {
            cmds.add("-m");
            cmds.add("fast");
        }
        int returnCode = exec(cmds);
        StopStatus stopStatus = returnCode == 0 ? StopStatus.STOP_OK : StopStatus.STOP_FAILED;
        log.info("Stop result: {}({})", stopStatus.toString(), returnCode);
        return stopStatus;
    }

    @Override
    public void start() {
        int exitCode = exec(getPgBin("pg_ctl"), "start", "-w", "-t", "300", "-D", getDb());
        log.info("Start: {}", exitCode == 0 ? "SUCCESSFUL" : String.format("FAILED(exitCode:%d)", exitCode));
        if (exitCode != 0)
            throw new PgCtlStartFailed(exitCode);
    }

    private int exec(String... tokens) {
        return exec(Arrays.asList(tokens));
    }

    private int exec(List<String> commandTokens) {
        try {
            ProcessBuilder command = new ProcessBuilder().command(commandTokens);
            command.environment().put("LANGUAGE", "EN");
            log.info("Running command: {}", String.join(" ", command.command()));
            log.info("in {}", command.directory());
            //log.info("with environment [{}]", Joiner.on(", ").withKeyValueSeparator("=").join(command.environment()));
            Process process = command.start();
            ProcessStreamGlobber globber = new ProcessStreamGlobber(process);
            globber.setName(new File(commandTokens.get(0)).getName());
            globber.startGlobber();
            int returnVal = process.waitFor();
            log.info("returned {}", returnVal);
            return returnVal;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void initDb() {
        initDb(DEFAULT_TCP_PORT);
    }

    @Override
    public boolean dbExist() {
        boolean result = getDbPath().toFile().exists();
        return result;
    }

    public void initDb(int tcpPort) {
        if (dbExist())
            throw new PgdataAlreadyExists(getDb());
        File pwFile = getPwFile();
        int exitValue;
        try {
            exitValue = exec(getPgBin("initdb"), "--no-locale", "-E", "UTF8", "-U", DsPostgreSqlProvider.USERNAME
                    , "-A", "md5", "-D", getDb(), "--pwfile=" + pwFile.getAbsolutePath());
        } finally {
            org.apache.commons.io.FileUtils.deleteQuietly(pwFile);
        }
        if (exitValue != 0)
            throw new RuntimeException("Could not initialize database, initdb returned " + exitValue);

        pgSqlConfigFix.fixConfig(getDbPath(), tcpPort);
    }

    private File getPwFile() {
        try {
            File pwFile = File.createTempFile("tmp", "tmp");
            FileUtils.write(pwFile, DsPostgreSqlProvider.PASSWORD);
            return pwFile;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private class PgdataAlreadyExists extends RuntimeException {
        public PgdataAlreadyExists(String message) {
            super(message);
        }
    }

    private class PgCtlStartFailed extends RuntimeException {
        public PgCtlStartFailed(int returnCode) {
            super("return code: " + returnCode);
        }
    }
}
