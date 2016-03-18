package de.neemann.digital.core.basic;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;

import java.util.ArrayList;

/**
 * @author hneemann
 */
public abstract class Function extends FanIn {

    private long value;

    public Function(int bits) {
        super(bits);
    }

    @Override
    public void readInputs() throws NodeException {
        value = calculate(inputs);
    }

    @Override
    public void writeOutputs() throws NodeException {
        output.setValue(value);
    }

    protected abstract int calculate(ArrayList<ObservableValue> inputs) throws NodeException;
}
