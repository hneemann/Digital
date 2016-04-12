package de.neemann.digital.core.basic;

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
public class XOr extends Node implements Element {

    public static final ElementTypeDescription DESCRIPTION
            = new ElementTypeDescription(XOr.class, "a", "b")
            .addAttribute(AttributeKey.Rotate)
            .addAttribute(AttributeKey.Bits);

    private final int bits;
    private final ObservableValue out;
    protected ObservableValue a;
    protected ObservableValue b;
    protected long value;

    public XOr(ElementAttributes attributes) {
        bits = attributes.get(AttributeKey.Bits);
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
