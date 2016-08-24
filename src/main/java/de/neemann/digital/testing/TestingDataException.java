package de.neemann.digital.testing;

/**
 * @author hneemann
 */
public class TestingDataException extends Exception {
    /**
     * creates a new instance
     *
     * @param cause the cause
     */
    public TestingDataException(Exception cause) {
        super(cause);
    }

    /**
     * creates a new instance
     *
     * @param message the message
     */
    public TestingDataException(String message) {
        super(message);
    }
}
