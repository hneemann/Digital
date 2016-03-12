package de.neemann.digital.wiring;

import de.neemann.digital.NodeException;
import de.neemann.digital.ObservableValue;
import de.neemann.digital.basic.FanIn;

/**
 * @author hneemann
 */
public class Multiplexer extends FanIn {

    private ObservableValue selector;
    private int value;

    public Multiplexer(int bits, ObservableValue selector) {
        super(bits);
        this.selector = selector;
        selector.addListener(this);
    }

    @Override
    public void readInputs() throws NodeException {
        int n = selector.getValueBits();
        if (n >= inputs.size())
            throw new NodeException("multiplexerSelectsNotPresentInput");

        value = inputs.get(n).getValue();
    }

    @Override
    public void writeOutputs() throws NodeException {
        output.setValue(value);
    }
}
