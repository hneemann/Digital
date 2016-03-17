package de.neemann.digital.core.arithmetic;

import de.neemann.digital.core.*;
import de.neemann.digital.gui.draw.shapes.GenericShape;

/**
 * @author hneemann
 */
public class Add extends Node implements Part {

    private final int bits;
    private final ObservableValue sum;
    private final ObservableValue c_out;
    private final long mask;
    protected ObservableValue a;
    protected ObservableValue b;
    protected ObservableValue c_in;
    protected long value;

    public Add(int bits) {
        this.bits = bits;
        this.mask = 1 << bits;

        this.sum = new ObservableValue("sum", bits);
        this.c_out = new ObservableValue("c_out", 1);
    }

    public static PartDescription createFactory(int bits) {
        return new PartDescription(new GenericShape("+", 2), new PartFactory() {
            @Override
            public Part create() {
                return new Add(bits);
            }
        }, "a", "b", "c_in");
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

    @Override
    public void setInputs(ObservableValue... inputs) throws BitsException {
        a = inputs[0];
        a.addListener(this);
        b = inputs[1];
        b.addListener(this);
        c_in = inputs[2];
        c_in.addListener(this);

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

    @Override
    public void registerNodes(Model model) {
        model.add(this);
    }
}
