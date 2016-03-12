package de.neemann.digital.core;

/**
 * @author hneemann
 */
public class BurnException extends NodeException {
    public BurnException(ObservableValue... values) {
        super("burnException", values);
    }
}
