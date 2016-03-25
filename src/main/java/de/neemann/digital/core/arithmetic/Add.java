package de.neemann.digital.core.arithmetic;

import de.neemann.digital.core.BitsException;
import de.neemann.digital.core.Node;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.AttributeKey;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;

/**
 * @author hneemann
 */
public class Add extends Node implements Element {

    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(Add.class, "a", "b", "c_i").addAttribute(AttributeKey.Bits);
    private final int bits;
    private final ObservableValue sum;
    private final ObservableValue c_out;
    private final long mask;
    protected ObservableValue a;
    protected ObservableValue b;
    protected ObservableValue c_in;
    protected long value;

    public Add(ElementAttributes attributes) {
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
        a = inputs[0].addObserver(this).checkBits(bits, this);
        b = inputs[1].addObserver(this).checkBits(bits, this);
        c_in = inputs[2].addObserver(this).checkBits(1, this);
    }

    @Override
    public ObservableValue[] getOutputs() {
        return new ObservableValue[]{sum, c_out};
    }

}
