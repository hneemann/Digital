package de.neemann.digital.core.arithmetic;

import de.neemann.digital.core.*;

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

    public static PartFactory createFactory(int bits) {
        return new PartFactory("a", "b") {
            @Override
            public Part create() {
                return new Mul(bits);
            }
        };
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
}
