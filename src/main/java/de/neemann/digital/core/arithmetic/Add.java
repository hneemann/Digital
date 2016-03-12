package de.neemann.digital.core.arithmetic;

import de.neemann.digital.core.BitsException;
import de.neemann.digital.core.Node;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;

/**
 * @author hneemann
 */
public class Add extends Node {

    protected final ObservableValue a;
    protected final ObservableValue b;
    protected final ObservableValue c_in;
    private final ObservableValue sum;
    private final ObservableValue c_out;
    private final long mask;
    protected long value;

    public Add(ObservableValue a, ObservableValue b, ObservableValue c_in) throws BitsException {
        this.a = a;
        this.b = b;
        this.c_in = c_in;

        if (a.getBits() != b.getBits())
            throw new BitsException("notSameBitCount", a, b);

        if (c_in.getBits() != 1)
            throw new BitsException("carryIsABit", c_in);

        int bits = a.getBits();
        this.mask = 1 << bits;

        a.addListener(this);
        b.addListener(this);
        c_in.addListener(this);

        this.sum = new ObservableValue(bits);
        this.c_out = new ObservableValue(1);
    }

    @Override
    public void readInputs() throws NodeException {
        value = a.getValueBits() + b.getValueBits() + c_in.getValueBits();
    }

    @Override
    public void writeOutputs() throws NodeException {
        sum.setValue(value);
        c_out.setValue((value & mask) == 0 ? 0 : 1);
    }

    public ObservableValue getSum() {
        return sum;
    }

    public ObservableValue getCOut() {
        return c_out;
    }

}
