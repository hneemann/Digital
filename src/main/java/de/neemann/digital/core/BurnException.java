package de.neemann.digital.core;

/**
 * @author hneemann
 */
public class BurnException extends NodeException {
    public BurnException(Node node, ObservableValue... values) {
        super("burnException", node, values);
    }
}
