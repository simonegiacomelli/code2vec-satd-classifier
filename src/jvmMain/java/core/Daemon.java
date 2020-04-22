package core;

/* Simone 25/08/13 8.25 */

public class Daemon {

    public void start(String name, Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        thread.setName(name);
        thread.start();
    }
}
