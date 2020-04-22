package core;

/* Simone 08/07/2014 12:47 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ProcessStreamGlobber {
    final static Logger log = LoggerFactory.getLogger(ProcessStreamGlobber.class);
    private Process process;
    private String name;

    public ProcessStreamGlobber(Process process) {
        this.process = process;
    }

    public void startGlobber() {
        startStreamGobbler(process.getErrorStream(), "ERR");
        startStreamGobbler(process.getInputStream(), "OUT");
    }

    private void startStreamGobbler(InputStream errorStream, String streamName) {
        StreamGobbler sg = new StreamGobbler(errorStream, streamName);
        if (name != null)
            sg.setName(name + "-" + streamName);
        sg.start();
    }

    public void setName(String name) {
        this.name = name;
    }

    class StreamGobbler extends Thread {
        InputStream is;
        String type;

        StreamGobbler(InputStream is, String type) {
            this.is = is;
            this.type = type;
        }

        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line;
                while ((line = br.readLine()) != null)
                    log.info(type + "> " + line);

            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
}
