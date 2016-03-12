package de.neemann.digital.core;

/**
 * @author hneemann
 */
public class NodeException extends Exception {
    private final ObservableValue[] values;

    public NodeException(String message, ObservableValue... values) {
        super(message);
        this.values = values;
    }

    public ObservableValue[] getValues() {
        return values;
    }
}
