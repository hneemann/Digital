package de.neemann.digital.draw.library;

/**
 * Thrown if a node is not valid, which means it is not allowed to attach a component to it
 */
public class InvalidNodeException extends Exception {
    /**
     * Creates a new instance
     *
     * @param message message
     */
    public InvalidNodeException(String message) {
        super(message);
    }
}
