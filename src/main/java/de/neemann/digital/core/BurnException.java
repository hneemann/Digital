package de.neemann.digital.core;

import de.neemann.digital.lang.Lang;

/**
 * Is thrown if more then one output of a set of connected outputs becomes active
 *
 * @author hneemann
 */
public class BurnException extends RuntimeException {
    /**
     * Creates a new instance
     */
    public BurnException() {
        super(Lang.get("err_burnError"));
    }
}
