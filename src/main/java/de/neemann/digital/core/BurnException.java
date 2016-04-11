package de.neemann.digital.core;

import de.neemann.digital.lang.Lang;

/**
 * Is thrown if more then one output of a set of connected outputs becomes active
 *
 * @author hneemann
 */
public class BurnException extends RuntimeException {
    private final ObservableValue value;

    /**
     * Creates a new instance
     */
    public BurnException(ObservableValue value) {
        super(Lang.get("err_burnError"));
        this.value = value;
    }

    /**
     * @return returns the causing value
     */
    public ObservableValue getValue() {
        return value;
    }
}
