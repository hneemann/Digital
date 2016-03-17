package de.neemann.digital.core;

/**
 * @author hneemann
 */
public class HighZException extends RuntimeException {

    public HighZException(ObservableValue... causedObservable) {
        super("readOfHighZ");
    }
}
