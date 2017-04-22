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
            = new ElementTypeDescription(Add.class, input("a"), input("b"), input("c_i"))
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

        this.sum = new ObservableValue("s", bits).setPinDescription(DESCRIPTION);
        this.cOut = new ObservableValue("c_o", 1).setPinDescription(DESCRIPTION);
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
        a = inputs.get(0).addObserverToValue(this).checkBits(bits, this, 0);
        b = inputs.get(1).addObserverToValue(this).checkBits(bits, this, 1);
        cIn = inputs.get(2).addObserverToValue(this).checkBits(1, this, 2);

        if (bits > 63)
            throw new BitsException(Lang.get("err_toManyBits_Found_N0_maxIs_N1", bits, 63), this, 0, new ObservableValues(a, b));
    }

    @Override
    public ObservableValues getOutputs() {
        return ovs(sum, cOut);
    }

}
