package de.neemann.digital.core;

/**
 * @author hneemann
 */
public class BitsException extends NodeException {
    public BitsException(String message, ObservableValue... values) {
        super(message, values);
    }
}
