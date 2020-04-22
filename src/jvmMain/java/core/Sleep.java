package core;

/* Simone 11/05/13 9.59 */

import org.joda.time.Duration;

public class Sleep  {
    public static Sleep instance = new Sleep();

    public void sleep(long millis) {
        sleepStatic(millis);
    }

    public static void sleepStatic(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void sleepStatic(Duration duration)
    {
        sleepStatic(duration.getMillis());
    }

    public static void forever() {
        while (true)
            sleepStatic(10000);
    }
}
