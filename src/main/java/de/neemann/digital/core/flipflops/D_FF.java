package de.neemann.digital.core.flipflops;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.AttributeKey;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;

/**
 * @author hneemann
 */
public class D_FF extends Node implements Element {

    public static final ElementTypeDescription DESCRIPTION
            = new ElementTypeDescription(D_FF.class, "D", "C")
            .addAttribute(AttributeKey.Rotate)
            .addAttribute(AttributeKey.Bits)
            .addAttribute(AttributeKey.Label)
            .addAttribute(AttributeKey.Default)
            .addAttribute(AttributeKey.ValueIsProbe)
            .setShortName("D");

    private final int bits;
    private final boolean isProbe;
    private final String label;
    private ObservableValue dVal;
    private ObservableValue clockVal;
    private ObservableValue q;
    private ObservableValue qn;
    private boolean lastClock;
    private long value;

    public D_FF(ElementAttributes attributes) {
        bits = attributes.getBits();
        this.q = new ObservableValue("Q", bits);
        this.qn = new ObservableValue("\u00ACQ", bits);
        isProbe = attributes.get(AttributeKey.ValueIsProbe);
        label = attributes.get(AttributeKey.Label);

        value = attributes.get(AttributeKey.Default);
        q.setValue(value);
        qn.setValue(~value);
    }

    @Override
    public void readInputs() throws NodeException {
        boolean clock = clockVal.getBool();
        if (clock && !lastClock)
            value = dVal.getValue();
        lastClock = clock;
    }

    @Override
    public void writeOutputs() throws NodeException {
        q.setValue(value);
        qn.setValue(~value);
    }

    @Override
    public void setInputs(ObservableValue... inputs) throws BitsException {
        dVal = inputs[0].addObserverToValue(this).checkBits(bits, this);
        clockVal = inputs[1].addObserverToValue(this).checkBits(1, this);
    }

    @Override
    public ObservableValue[] getOutputs() {
        return new ObservableValue[]{q, qn};
    }

    @Override
    public void registerNodes(Model model) {
        super.registerNodes(model);
        if (isProbe)
            model.addSignal(label, q);
    }

}
