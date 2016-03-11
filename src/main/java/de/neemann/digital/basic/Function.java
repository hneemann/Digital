package de.neemann.digital.basic;

import de.neemann.digital.BitsException;
import de.neemann.digital.Node;
import de.neemann.digital.NodeException;
import de.neemann.digital.ObservableValue;

import java.util.ArrayList;

/**
 * @author hneemann
 */
public abstract class Function extends Node {

    private final ArrayList<ObservableValue> inputs;
    private final ObservableValue output;
    private int value;

    public Function(int bits) {
        output = new ObservableValue(bits);
        inputs = new ArrayList<>();
    }

    public Function addInput(ObservableValue value) throws BitsException, NodeException {
        output.checkBits(value);
        inputs.add(value);
        value.addListener(this);
        return this;
    }

    public void removeInput(ObservableValue value) {
        inputs.remove(value);
        value.removeListener(this);
    }

    public ObservableValue getOutput() {
        return output;
    }


    @Override
    public void readInputs() throws NodeException {
        value = calculate(inputs);
    }

    @Override
    public void writeOutputs() throws NodeException {
        output.setValue(value);
    }

    protected abstract int calculate(ArrayList<ObservableValue> inputs);
}
