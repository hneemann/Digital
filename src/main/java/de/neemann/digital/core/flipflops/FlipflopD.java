package de.neemann.digital.core.flipflops;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * The D Flipflop
 *
 * @author hneemann
 */
public class FlipflopD extends Node implements Element {

    /**
     * The D-FF description
     */
    public static final ElementTypeDescription DESCRIPTION
            = new ElementTypeDescription("D_FF", FlipflopD.class, input("D"), input("C"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.BITS)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.DEFAULT)
            .addAttribute(Keys.VALUE_IS_PROBE)
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

    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     */
    public FlipflopD(ElementAttributes attributes) {
        bits = attributes.getBits();
        this.q = new ObservableValue("Q", bits);
        this.qn = new ObservableValue("\u00ACQ", bits);
        isProbe = attributes.get(Keys.VALUE_IS_PROBE);
        label = attributes.getCleanLabel();

        value = attributes.get(Keys.DEFAULT);
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
    public void setInputs(ObservableValues inputs) throws BitsException {
        dVal = inputs.get(0).addObserverToValue(this).checkBits(bits, this);
        clockVal = inputs.get(1).addObserverToValue(this).checkBits(1, this);
    }

    @Override
    public ObservableValues getOutputs() {
        return new ObservableValues(q, qn);
    }

    @Override
    public void registerNodes(Model model) {
        super.registerNodes(model);
        if (isProbe)
            model.addSignal(label, q);
    }

}
