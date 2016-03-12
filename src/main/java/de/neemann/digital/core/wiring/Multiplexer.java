package de.neemann.digital.core.wiring;

import de.neemann.digital.core.BitsException;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.basic.FanIn;

/**
 * @author hneemann
 */
public class Multiplexer extends FanIn {

    private ObservableValue selector;
    private long value;

    public Multiplexer(int bits, ObservableValue selector) {
        super(bits);
        this.selector = selector;
        selector.addListener(this);
    }

    @Override
    public void readInputs() throws NodeException {
        int n = (int) selector.getValueBits();
        if (n >= inputs.size())
            throw new NodeException("multiplexerSelectsNotPresentInput");

        value = inputs.get(n).getValue();
    }

    @Override
    public void writeOutputs() throws NodeException {
        output.setValue(value);
    }

    @Override
    public void checkConsistence() throws NodeException {
        super.checkConsistence();

        if ((1 << selector.getBits()) != inputs.size())
            throw new BitsException("selectorMismatch", selector);
    }
}
