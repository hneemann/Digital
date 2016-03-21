package de.neemann.digital.core.wiring;

import de.neemann.digital.core.BitsException;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.basic.FanIn;

import java.util.Arrays;

/**
 * @author hneemann
 */
public class Multiplexer extends FanIn {

    private final int selectorBits;
    private ObservableValue selector;
    private long value;

    public Multiplexer(int dataBits, int selectorBits) {
        super(dataBits);
        this.selectorBits = selectorBits;
    }

    @Override
    public void readInputs() throws NodeException {
        int n = (int) selector.getValue();
        if (n >= inputs.size())
            throw new NodeException("multiplexerSelectsNotPresentInput", this);

        value = inputs.get(n).getValue();
    }

    @Override
    public void writeOutputs() throws NodeException {
        output.setValue(value);
    }

    @Override
    public void setInputs(ObservableValue... inputs) throws NodeException {
        selector = inputs[inputs.length - 1];
        selector.addObserver(this);
        super.setInputs(Arrays.copyOfRange(inputs, 0, inputs.length - 1));

        if (selector.getBits() != selectorBits)
            throw new BitsException("selectorMismatch", this, selector);
    }
}
