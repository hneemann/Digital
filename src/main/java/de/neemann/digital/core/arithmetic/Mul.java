package de.neemann.digital.core.arithmetic;

import de.neemann.digital.core.BitsException;
import de.neemann.digital.core.Node;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;

/**
 * @author hneemann
 */
public class Mul extends Node {

    private final ObservableValue a;
    private final ObservableValue b;
    private final ObservableValue mul;
    private long value;

    public Mul(ObservableValue a, ObservableValue b) throws BitsException {
        this.a = a;
        this.b = b;

        a.addListener(this);
        b.addListener(this);

        this.mul = new ObservableValue(a.getBits() + b.getBits());
    }

    @Override
    public void readInputs() throws NodeException {
        value = a.getValueBits() * b.getValueBits();
    }

    @Override
    public void writeOutputs() throws NodeException {
        mul.setValue(value);
    }

    public ObservableValue getMul() {
        return mul;
    }
}
