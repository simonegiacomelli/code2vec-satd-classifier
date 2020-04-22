package core;

/* Simone 08/04/2014 16:22 */

import java.io.IOException;

public class RuntimeIOException extends RuntimeException {
    public RuntimeIOException(IOException e) {
        super(e);
    }
}
