package de.neemann.digital.core;

import java.util.List;

/**
 * Is thrown if more then one output of a set of connected outputs becomes active
 *
 * @author hneemann
 */
public class BurnException extends RuntimeException {
    private final List<ObservableValue> values;

    /**
     * Creates a new instance
     *
     * @param message the message
     * @param values  the values connected to the net
     */
    public BurnException(String message, List<ObservableValue> values) {
        super(message);
        this.values = values;
    }

    /**
     * @return returns the causing value
     */
    public List<ObservableValue> getValues() {
        return values;
    }
}
