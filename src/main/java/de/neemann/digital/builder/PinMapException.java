package de.neemann.digital.builder;

/**
 * @author hneemann
 */
public class PinMapException extends Exception {
    /**
     * Creates a new instance
     *
     * @param message the message
     */
    public PinMapException(String message) {
        super(message);
    }

    /**
     * Creates a new instance
     *
     * @param e the causing exception
     */
    public PinMapException(Exception e) {
        super(e);
    }
}
