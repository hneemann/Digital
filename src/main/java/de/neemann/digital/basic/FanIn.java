package de.neemann.digital.basic;

import de.neemann.digital.BitsException;
import de.neemann.digital.Node;
import de.neemann.digital.NodeException;
import de.neemann.digital.ObservableValue;

import java.util.ArrayList;

/**
 * @author hneemann
 */
public abstract class FanIn extends Node {
    protected final ArrayList<ObservableValue> inputs;
    protected final ObservableValue output;

    public FanIn(int bits) {
        inputs = new ArrayList<>();
        output = new ObservableValue(bits);
    }

    public FanIn addInput(ObservableValue value) throws BitsException, NodeException {
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
}
