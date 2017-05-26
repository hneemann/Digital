package de.neemann.digital.testing;

/**
 * @author hneemann
 */
public class TestingDataException extends Exception {
    /**
     * creates a new instance
     *
     * @param message the error message
     * @param cause   the cause
     */
    public TestingDataException(String message, Exception cause) {
        super(message, cause);
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
