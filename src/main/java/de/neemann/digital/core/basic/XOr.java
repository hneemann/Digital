package de.neemann.digital.core.basic;

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
public class XOr extends Node implements Part {

    public static final PartTypeDescription DESCRIPTION = new PartTypeDescription(XOr.class, "a", "b").addAttribute(AttributeKey.Bits);
    private final int bits;
    private final ObservableValue out;
    protected ObservableValue a;
    protected ObservableValue b;
    protected long value;

    public XOr(PartAttributes attributes) {
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
        a = inputs[0];
        a.addObserver(this);
        b = inputs[1];
        b.addObserver(this);

        if (a.getBits() != bits)
            throw new BitsException("wrongBitCount", a);

        if (b.getBits() != bits)
            throw new BitsException("wrongBitCount", b);
    }

    @Override
    public ObservableValue[] getOutputs() {
        return new ObservableValue[]{out};
    }

}
