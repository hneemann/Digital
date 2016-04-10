package de.neemann.digital.core;

import de.neemann.digital.lang.Lang;

/**
 * Is thrown if a high z value is read.
 *
 * @author hneemann
 */
public class HighZException extends RuntimeException {

    /**
     * Creates a new instance
     *
     * @param causedObservable the affected value
     */
    public HighZException(ObservableValue... causedObservable) {
        super(Lang.get("err_readOfHighZ"));
    }
}
