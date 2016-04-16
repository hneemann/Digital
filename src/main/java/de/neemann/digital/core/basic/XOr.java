package de.neemann.digital.core.basic;

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
 * @author hneemann
 */
public class XOr extends Node implements Element {

    /**
     * The XOr description
     */
    public static final ElementTypeDescription DESCRIPTION
            = new ElementTypeDescription(XOr.class, input("a"), input("b"))
            .addAttribute(Keys.Rotate)
            .addAttribute(Keys.Bits);

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
        bits = attributes.get(Keys.Bits);
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
    public void setInputs(ObservableValue... inputs) throws BitsException {
        a = inputs[0].addObserverToValue(this).checkBits(bits, this);
        b = inputs[1].addObserverToValue(this).checkBits(bits, this);
    }

    @Override
    public ObservableValue[] getOutputs() {
        return new ObservableValue[]{out};
    }

}
