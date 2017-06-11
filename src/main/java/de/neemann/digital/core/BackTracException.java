package de.neemann.digital.core;

/**
 * Indicates an error backtracking a value to all affected values
 * Created by hneemann on 11.06.17.
 */
public class BackTracException extends Exception {

    /**
     * Creates a new instance
     *
     * @param message the message
     */
    public BackTracException(String message) {
        super(message);
    }
}
