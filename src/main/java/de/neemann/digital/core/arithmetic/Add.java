package de.neemann.digital.core.arithmetic;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.lang.Lang;

import static de.neemann.digital.core.ObservableValues.ovs;
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
            = new ElementTypeDescription(Add.class,
            input("a", Lang.get("elem_Add_input1")),
            input("b", Lang.get("elem_Add_input2")),
            input("c_i", Lang.get("elem_Add_carryIn")))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.BITS);

    private final int bits;
    private final ObservableValue sum;
    private final ObservableValue cOut;
    private final long mask;
    private ObservableValue a;
    private ObservableValue b;
    private ObservableValue cIn;
    private long value;

    /**
     * Create a new instance
     *
     * @param attributes the attributes
     */
    public Add(ElementAttributes attributes) {
        bits = attributes.get(Keys.BITS);
        this.mask = 1 << bits;

        this.sum = new ObservableValue("s", bits).setDescription(Lang.get("elem_Add_output"));
        this.cOut = new ObservableValue("c_o", 1).setDescription(Lang.get("elem_Add_carryOut"));
    }

    @Override
    public void readInputs() throws NodeException {
        value = calc(a.getValue(), b.getValue(), cIn.getValue());
    }

    /**
     * Performs the add operation
     *
     * @param a a
     * @param b b
     * @param c carry
     * @return the result
     */
    protected long calc(long a, long b, long c) {
        return a + b + c;
    }

    @Override
    public void writeOutputs() throws NodeException {
        sum.setValue(value);
        cOut.setValue((value & mask) == 0 ? 0 : 1);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws BitsException {
        a = inputs.get(0).addObserverToValue(this).checkBits(bits, this);
        b = inputs.get(1).addObserverToValue(this).checkBits(bits, this);
        cIn = inputs.get(2).addObserverToValue(this).checkBits(1, this);
    }

    @Override
    public ObservableValues getOutputs() {
        return ovs(sum, cOut);
    }

}
