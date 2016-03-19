package de.neemann.digital.core.arithmetic;

import de.neemann.digital.core.BitsException;
import de.neemann.digital.core.Node;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.part.AttributeKey;
import de.neemann.digital.core.part.Part;
import de.neemann.digital.core.part.PartAttributes;
import de.neemann.digital.core.part.PartTypeDescription;

/**
 * @author hneemann
 */
public class Add extends Node implements Part {

    public static final PartTypeDescription DESCRIPTION = new PartTypeDescription(Add.class, "a", "b", "c_i").addAttribute(AttributeKey.Bits);
    private final int bits;
    private final ObservableValue sum;
    private final ObservableValue c_out;
    private final long mask;
    protected ObservableValue a;
    protected ObservableValue b;
    protected ObservableValue c_in;
    protected long value;

    public Add(PartAttributes attributes) {
        bits = attributes.get(AttributeKey.Bits);
        this.mask = 1 << bits;

        this.sum = new ObservableValue("s", bits);
        this.c_out = new ObservableValue("c_o", 1);
    }

    @Override
    public void readInputs() throws NodeException {
        value = a.getValue() + b.getValue() + c_in.getValue();
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

    @Override
    public void setInputs(ObservableValue... inputs) throws BitsException {
        a = inputs[0].addObserver(this);
        b = inputs[1].addObserver(this);
        c_in = inputs[2].addObserver(this);

        if (a.getBits() != bits)
            throw new BitsException("wrongBitCount", a);

        if (b.getBits() != bits)
            throw new BitsException("wrongBitCount", b);

        if (c_in.getBits() != 1)
            throw new BitsException("carryIsABit", c_in);
    }

    @Override
    public ObservableValue[] getOutputs() {
        return new ObservableValue[]{sum, c_out};
    }

}
