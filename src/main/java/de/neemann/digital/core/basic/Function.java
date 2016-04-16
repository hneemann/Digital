package de.neemann.digital.core.basic;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;

import java.util.ArrayList;

/**
 * @author hneemann
 */
abstract class Function extends FanIn {

    private long value;

    Function(int bits) {
        super(bits);
    }

    @Override
    public void readInputs() throws NodeException {
        value = calculate(getInputs());
    }

    @Override
    public void writeOutputs() throws NodeException {
        getOutput().setValue(value);
    }

    protected abstract int calculate(ArrayList<ObservableValue> inputs) throws NodeException;

}
