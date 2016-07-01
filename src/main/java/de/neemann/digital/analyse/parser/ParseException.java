package de.neemann.digital.analyse.parser;

/**
 * Exception thrown during parsing of expression
 *
 * @author hneemann
 */
public class ParseException extends Exception {
    /**
     * Creates a new instance
     *
     * @param message the message
     */
    public ParseException(String message) {
        super(message);
    }
}
