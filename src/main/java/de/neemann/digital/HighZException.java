package de.neemann.digital;

/**
 * @author hneemann
 */
public class HighZException extends NodeException {

    public HighZException(ObservableValue... causedObservable) {
        super("readOfHighZ", causedObservable);
    }
}
