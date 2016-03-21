package de.neemann.digital.core;

/**
 * @author hneemann
 */
public class BitsException extends NodeException {
    public BitsException(String message, Node node, ObservableValue... values) {
        super(message, node, values);
    }
}
