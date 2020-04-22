package core;

/* Simone 04/09/13 11.30 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ignore {
    final static Logger log = LoggerFactory.getLogger(Ignore.class);

    public static void exception(RunnableEx runnable) {
        try {
            runnable.run();
        } catch (Exception ex) {
            log.error("", ex);
        }
    }

    public static void exception(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception ex) {
            log.error("", ex);
        }
    }
}
