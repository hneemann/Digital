package de.neemann.digital.core;

import de.neemann.digital.lang.Lang;

/**
 * @author hneemann
 */
public class HighZException extends RuntimeException {

    public HighZException(ObservableValue... causedObservable) {
        super(Lang.get("err_readOfHighZ"));
    }
}
