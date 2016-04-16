package de.neemann.digital.core.arithmetic;

import de.neemann.digital.core.BitsException;
import de.neemann.digital.core.Node;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * A adder.
 *
 * @author hneemann
 */
public class Add extends Node implements Element {

    /**
     * The adders description
     */
    public static final ElementTypeDescription DESCRIPTION
            = new ElementTypeDescription(Add.class, input("a"), input("b"), input("c_i"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.BITS);

    private final int bits;
    private final ObservableValue sum;
    private final ObservableValue cOut;
    private final long mask;
    protected ObservableValue a;
    protected ObservableValue b;
    protected ObservableValue cIn;
    protected long value;

    /**
     * Create a new instance
     *
     * @param attributes the attributes
     */
    public Add(ElementAttributes attributes) {
        bits = attributes.get(Keys.BITS);
        this.mask = 1 << bits;

        this.sum = new ObservableValue("s", bits);
        this.cOut = new ObservableValue("c_o", 1);
    }

    @Override
    public void readInputs() throws NodeException {
        value = a.getValue() + b.getValue() + cIn.getValue();
    }

    @Override
    public void writeOutputs() throws NodeException {
        sum.setValue(value);
        cOut.setValue((value & mask) == 0 ? 0 : 1);
    }

    @Override
    public void setInputs(ObservableValue... inputs) throws BitsException {
        a = inputs[0].addObserverToValue(this).checkBits(bits, this);
        b = inputs[1].addObserverToValue(this).checkBits(bits, this);
        cIn = inputs[2].addObserverToValue(this).checkBits(1, this);
    }

    @Override
    public ObservableValue[] getOutputs() {
        return new ObservableValue[]{sum, cOut};
    }

}
