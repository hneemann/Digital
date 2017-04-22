package de.neemann.digital.core.arithmetic;

import de.neemann.digital.core.Node;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * A multiplier
 *
 * @author hneemann
 */
public class Mul extends Node implements Element {

    /**
     * The multiplier description
     */
    public static final ElementTypeDescription DESCRIPTION
            = new ElementTypeDescription(Mul.class, input("a"), input("b"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.BITS);

    private final ObservableValue mul;
    private final int bits;
    private ObservableValue a;
    private ObservableValue b;
    private long value;

    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     */
    public Mul(ElementAttributes attributes) {
        bits = attributes.get(Keys.BITS);
        this.mul = new ObservableValue("mul", bits * 2).setPinDescription(DESCRIPTION);
    }

    @Override
    public void readInputs() throws NodeException {
        value = a.getValue() * b.getValue();
    }

    @Override
    public void writeOutputs() throws NodeException {
        mul.setValue(value);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        a = inputs.get(0).addObserverToValue(this).checkBits(bits, this, 0);
        b = inputs.get(1).addObserverToValue(this).checkBits(bits, this, 1);
    }

    @Override
    public ObservableValues getOutputs() {
        return mul.asList();
    }

}
