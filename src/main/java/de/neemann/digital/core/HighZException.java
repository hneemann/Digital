package de.neemann.digital.core;

/**
 * @author hneemann
 */
public class HighZException extends NodeException {

    public HighZException(ObservableValue... causedObservable) {
        super("readOfHighZ", causedObservable);
    }
}
