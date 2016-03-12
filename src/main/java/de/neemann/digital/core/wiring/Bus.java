package de.neemann.digital.core.wiring;

import de.neemann.digital.core.BurnException;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.Value;
import de.neemann.digital.core.basic.FanIn;

/**
 * @author hneemann
 */
public class Bus extends FanIn {

    private Value value;

    public Bus(int bits) {
        super(bits);
        value = new Value(bits);
    }

    @Override
    public void readInputs() throws NodeException {
        ObservableValue found = null;
        for (ObservableValue in : inputs) {
            if (!in.isHighZ()) {
                if (found != null)
                    throw new BurnException(in, found);
                found = in;
            }
        }
        if (found == null) {
            value.set(0, true);
        } else {
            value.set(found.getValue(), false);
        }
    }

    @Override
    public void writeOutputs() throws NodeException {
        output.set(value);
    }
}
