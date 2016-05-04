package de.neemann.digital.analyse.expression.format;

/**
 * Error thrown if there is an formatting error
 *
 * @author hneemann
 */
public class FormatterException extends Exception {
    /**
     * Creates a new instance
     *
     * @param message the error message
     */
    public FormatterException(String message) {
        super(message);
    }
}
