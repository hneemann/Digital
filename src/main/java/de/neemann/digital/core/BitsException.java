package de.neemann.digital.core;

/**
 * Is thrown if bit count is not matching
 *
 * @author hneemann
 */
public class BitsException extends NodeException {
    /**
     * Creates a new instance
     *
     * @param message the message
     * @param node    the affected node
     * @param values  the affected values
     */
    public BitsException(String message, Node node, ObservableValue... values) {
        super(message, node, values);
    }
}
