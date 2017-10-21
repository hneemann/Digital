package de.neemann.digital.core.flipflops;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;

import static de.neemann.digital.core.ObservableValues.ovs;
import static de.neemann.digital.core.element.PinInfo.input;

/**
 * The D Flipflop
 *
 * @author hneemann
 */
public class FlipflopDAsync extends Node implements Element {

    /**
     * The D-FF description
     */
    public static final ElementTypeDescription DESCRIPTION
            = new ElementTypeDescription("D_FF_AS", FlipflopDAsync.class, input("Set"), input("D"), input("C"), input("Clr"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.BITS)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.DEFAULT)
            .addAttribute(Keys.INVERTER_CONFIG)
            .addAttribute(Keys.VALUE_IS_PROBE);

    private final int bits;
    private final boolean isProbe;
    private final String label;
    private ObservableValue setVal;
    private ObservableValue clrVal;
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
    public FlipflopDAsync(ElementAttributes attributes) {
        this(attributes,
                new ObservableValue("Q", attributes.getBits()).setPinDescription(DESCRIPTION),
                new ObservableValue("\u00ACQ", attributes.getBits()).setPinDescription(DESCRIPTION));
    }

    /**
     * Creates a new D-FF with the given outputs!
     *
     * @param label the label
     * @param q     output
     * @param qn    inverted output
     */
    public FlipflopDAsync(String label, ObservableValue q, ObservableValue qn) {
        this(new ElementAttributes().set(Keys.LABEL, label).setBits(q.getBits()), q, qn);
        if (qn.getBits() != q.getBits())
            throw new RuntimeException("wrong bit count given!");
    }

    private FlipflopDAsync(ElementAttributes attributes, ObservableValue q, ObservableValue qn) {
        super(true);
        bits = attributes.getBits();
        this.q = q;
        this.qn = qn;
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

        if (setVal.getBool()) value = -1;
        else if (clrVal.getBool()) value = 0;
    }

    @Override
    public void writeOutputs() throws NodeException {
        q.setValue(value);
        qn.setValue(~value);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws BitsException {
        setVal = inputs.get(0).addObserverToValue(this).checkBits(1, this, 0);
        dVal = inputs.get(1).checkBits(bits, this, 1);
        clockVal = inputs.get(2).addObserverToValue(this).checkBits(1, this, 2);
        clrVal = inputs.get(3).addObserverToValue(this).checkBits(1, this, 3);
    }

    @Override
    public ObservableValues getOutputs() {
        return ovs(q, qn);
    }

    @Override
    public void registerNodes(Model model) {
        super.registerNodes(model);
        if (isProbe)
            model.addSignal(new Signal(label, q));
    }

}
