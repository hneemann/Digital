package de.neemann.digital;

/**
 * @author hneemann
 */
public class BitsException extends NodeException {
    public BitsException(String message, ObservableValue... values) {
        super(message, values);
    }
}
