package de.neemann.digital.core.basic;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * @author hneemann
 */
public class XOr extends Node implements Element {

    /**
     * The XOr description
     */
    public static final ElementTypeDescription DESCRIPTION
            = new ElementTypeDescription(XOr.class, input("a"), input("b"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.BITS);

    private final int bits;
    private final ObservableValue out;
    protected ObservableValue a;
    protected ObservableValue b;
    protected long value;

    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     */
    public XOr(ElementAttributes attributes) {
        bits = attributes.get(Keys.BITS);
        this.out = new ObservableValue("out", bits);
    }

    @Override
    public void readInputs() throws NodeException {
        value = a.getValue() ^ b.getValue();
    }

    @Override
    public void writeOutputs() throws NodeException {
        out.setValue(value);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws BitsException {
        a = inputs.get(0).addObserverToValue(this).checkBits(bits, this);
        b = inputs.get(1).addObserverToValue(this).checkBits(bits, this);
    }

    @Override
    public ObservableValues getOutputs() {
        return new ObservableValues(out);
    }

}
