package de.neemann.digital.gui.components.test;

/**
 * @author hneemann
 */
public class DataException extends Exception {
    /**
     * creates a new instance
     *
     * @param cause the cause
     */
    public DataException(Exception cause) {
        super(cause);
    }

    /**
     * creates a new instance
     *
     * @param message the message
     */
    public DataException(String message) {
        super(message);
    }
}
