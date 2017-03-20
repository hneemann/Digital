package de.neemann.digital.core;

/**
 * Is thrown if more then one output of a set of connected outputs becomes active
 *
 * @author hneemann
 */
public class BurnException extends RuntimeException {
    private final ObservableValues values;

    /**
     * Creates a new instance
     *
     * @param message the message
     * @param values  the values connected to the net
     */
    public BurnException(String message, ObservableValues values) {
        super(message);
        this.values = values;
    }

    /**
     * @return returns the causing value
     */
    public ObservableValues getValues() {
        return values;
    }
}
