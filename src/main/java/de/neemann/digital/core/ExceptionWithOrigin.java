package de.neemann.digital.core;

import java.io.File;

/**
 * A exception which has a file as an origin
 * Created by hneemann on 16.06.17.
 */
public class ExceptionWithOrigin extends Exception {
    private File origin;

    /**
     * Creates a new exception
     *
     * @param message message
     */
    public ExceptionWithOrigin(String message) {
        super(message);
    }

    /**
     * Creates a new exception
     *
     * @param message message
     * @param cause   the cause
     */
    public ExceptionWithOrigin(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new exception
     *
     * @param cause the cause
     */
    public ExceptionWithOrigin(Throwable cause) {
        super(cause);
    }

    /**
     * @return the origin of the error
     */
    public File getOrigin() {
        return origin;
    }

    /**
     * Sets the origin of an error
     *
     * @param origin the file which causes the exception
     */
    public void setOrigin(File origin) {
        if (getOrigin() == null)
            this.origin = origin;
    }
}
