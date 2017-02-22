package de.neemann.digital.core;

import de.neemann.digital.lang.Lang;

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
     */
    public BurnException(List<ObservableValue> values) {
        super(Lang.get("err_burnError"));
        this.values = values;
    }

    /**
     * @return returns the causing value
     */
    public List<ObservableValue> getValues() {
        return values;
    }
}
