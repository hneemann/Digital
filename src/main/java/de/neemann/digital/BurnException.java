package de.neemann.digital;

/**
 * @author hneemann
 */
public class BurnException extends NodeException {
    private final ObservableValue v1;
    private final ObservableValue v2;

    public BurnException(ObservableValue v1, ObservableValue v2) {
        super("burnException");
        this.v1 = v1;
        this.v2 = v2;
    }

    public ObservableValue getV1() {
        return v1;
    }

    public ObservableValue getV2() {
        return v2;
    }
}
