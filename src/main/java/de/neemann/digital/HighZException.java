package de.neemann.digital;

/**
 * @author hneemann
 */
public class HighZException extends NodeException {
    private final ObservableValue causedObservable;

    public HighZException(ObservableValue causedObservable) {
        super("readOfHighZ");
        this.causedObservable = causedObservable;
    }

    public ObservableValue getCausedObservable() {
        return causedObservable;
    }
}
