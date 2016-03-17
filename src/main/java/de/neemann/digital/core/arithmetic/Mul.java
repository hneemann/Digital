package de.neemann.digital.core.arithmetic;

import de.neemann.digital.core.*;
import de.neemann.digital.gui.draw.shapes.GenericShape;

/**
 * @author hneemann
 */
public class Mul extends Node implements Part {

    private final ObservableValue mul;
    private ObservableValue a;
    private ObservableValue b;
    private long value;

    public Mul(int bits) {
        this.mul = new ObservableValue("mul", bits * 2);
    }

    public static PartDescription createFactory(int bits) {
        return new PartDescription(new GenericShape("*", 2), new PartFactory() {
            @Override
            public Part create() {
                return new Mul(bits);
            }
        }, "a", "b");
    }

    @Override
    public void readInputs() throws NodeException {
        value = a.getValueBits() * b.getValueBits();
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
        a.addListener(this);
        b = inputs[1];
        b.addListener(this);
    }

    @Override
    public ObservableValue[] getOutputs() {
        return new ObservableValue[]{mul};
    }

    @Override
    public void registerNodes(Model model) {
        model.add(this);
    }
}
