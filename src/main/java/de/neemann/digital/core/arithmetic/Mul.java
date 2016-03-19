package de.neemann.digital.core.arithmetic;

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
public class Mul extends Node implements Part {

    public static final PartTypeDescription DESCRIPTION = new PartTypeDescription(Mul.class, "a", "b").addAttribute(AttributeKey.Bits);
    private final ObservableValue mul;
    private ObservableValue a;
    private ObservableValue b;
    private long value;

    public Mul(PartAttributes attributes) {
        this.mul = new ObservableValue("mul", attributes.get(AttributeKey.Bits) * 2);
    }

    @Override
    public void readInputs() throws NodeException {
        value = a.getValue() * b.getValue();
    }

    @Override
    public void writeOutputs() throws NodeException {
        mul.setValue(value);
    }

    public ObservableValue getMul() {
        return mul;
    }

    @Override
    public void setInputs(ObservableValue... inputs) throws NodeException {
        a = inputs[0];
        a.addObserver(this);
        b = inputs[1];
        b.addObserver(this);
    }

    @Override
    public ObservableValue[] getOutputs() {
        return new ObservableValue[]{mul};
    }

}
