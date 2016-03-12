package de.neemann.digital.wiring;

import de.neemann.digital.BurnException;
import de.neemann.digital.NodeException;
import de.neemann.digital.ObservableValue;
import de.neemann.digital.Value;
import de.neemann.digital.basic.FanIn;

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
