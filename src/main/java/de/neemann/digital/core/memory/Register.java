package de.neemann.digital.core.memory;

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
public class Register extends Node implements Element {

    public static final ElementTypeDescription DESCRIPTION
            = new ElementTypeDescription(Register.class, "D", "C", "en")
            .addAttribute(AttributeKey.Rotate)
            .addAttribute(AttributeKey.Bits)
            .addAttribute(AttributeKey.Label)
            .setShortName("D");

    private final int bits;
    private ObservableValue dVal;
    private ObservableValue clockVal;
    private ObservableValue enableVal;
    private ObservableValue q;
    private boolean lastClock;
    private long value;

    public Register(ElementAttributes attributes) {
        bits = attributes.getBits();
        this.q = new ObservableValue("Q", bits);
    }

    @Override
    public void readInputs() throws NodeException {
        boolean enable = enableVal.getBool();
        boolean clock = clockVal.getBool();
        if (clock && !lastClock && enable)
            value = dVal.getValue();
        lastClock = clock;
    }

    @Override
    public void writeOutputs() throws NodeException {
        q.setValue(value);
    }

    @Override
    public void setInputs(ObservableValue... inputs) throws BitsException {
        dVal = inputs[0].addObserver(this).checkBits(bits, this);
        clockVal = inputs[1].addObserver(this).checkBits(1, this);
        enableVal = inputs[2].addObserver(this).checkBits(1, this);
    }

    @Override
    public ObservableValue[] getOutputs() {
        return new ObservableValue[]{q};
    }

}
