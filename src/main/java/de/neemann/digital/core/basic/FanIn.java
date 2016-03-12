package de.neemann.digital.core.basic;

import de.neemann.digital.core.BitsException;
import de.neemann.digital.core.Node;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;

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

    public FanIn addInput(ObservableValue value) throws NodeException {
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
    public void checkConsistence() throws NodeException {
        super.checkConsistence();
        for (ObservableValue in : inputs)
            if (in.getBits() != output.getBits())
                throw new BitsException("bitsMismatch", in, output);
    }
}
