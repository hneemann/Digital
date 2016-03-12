package de.neemann.digital.basic;

import de.neemann.digital.NodeException;
import de.neemann.digital.ObservableValue;

import java.util.ArrayList;

/**
 * @author hneemann
 */
public abstract class Function extends FanIn {

    private int value;

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
