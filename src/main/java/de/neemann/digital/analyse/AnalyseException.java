package de.neemann.digital.analyse;

/**
 * Exception thrown if there problems analysing the circuit
 *
 * @author hneemann
 */
public class AnalyseException extends Exception {
    /**
     * Creates a new instance
     *
     * @param message the message
     */
    public AnalyseException(String message) {
        super(message);
    }

    /**
     * Creates a new instance
     *
     * @param e the cause
     */
    public AnalyseException(Exception e) {
        super(e);
    }
}
