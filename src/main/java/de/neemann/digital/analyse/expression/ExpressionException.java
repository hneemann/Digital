package de.neemann.digital.analyse.expression;

/**
 * Error thrown during evaluation of an expression
 *
 * @author hneemann
 */
public class ExpressionException extends Exception {
    /**
     * Creates a new instance
     *
     * @param message the message
     */
    public ExpressionException(String message) {
        super(message);
    }
}
