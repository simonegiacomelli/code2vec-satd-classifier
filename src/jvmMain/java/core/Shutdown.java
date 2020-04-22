package core;

/* Simone 11/10/13 12.32 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class Shutdown {

    final static Logger log = LoggerFactory.getLogger(Shutdown.class);

    static Shutdown def;
    volatile boolean stopping;
    ArrayList<Runnable> listeners = new ArrayList<>();

    protected Shutdown() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                notifyAllRemaining();
            }

        });
    }

    public static synchronized Shutdown def() {
        if (def == null) def = new Shutdown();
        return def;
    }

    public boolean stopping() {
        return stopping;
    }

    public void addApplicationShutdownHook(Runnable runnable) {
        listeners.add(runnable);
    }

    private void notifyAllRemaining() {
        stopping = true;
        for (Runnable runnable : listeners.toArray(new Runnable[0])) {
            Ignore.exception(runnable);
            listeners.remove(runnable);
        }
    }

    public static void exit(int exitCode) {
        def().exitInternal(exitCode);
    }

    private synchronized void exitInternal(int exitCode) {
        if (stopping) {
            log.info("Stop already issued; exit code [{}] swallowed", exitCode);
            return;
        }

        log.info("Shutdown.exit({})", exitCode);
        def().notifyAllRemaining();
        System.exit(exitCode);

    }

    public void hook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                log.info("Shutting down");
            }
        });
        new Daemon().start("Shutdown safeguard watch", new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (Shutdown.def().stopping()) {
                        Sleep.sleepStatic(30000);
                        log.warn("Still alive. Halting");
                        Runtime.getRuntime().halt(4);
                        return;
                    }
                    Sleep.sleepStatic(1000);
                }
            }
        });
    }

    public boolean isShuttingDown() {
        return stopping;
    }
}
