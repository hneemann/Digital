package de.neemann.digital.core.flipflops;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;

import static de.neemann.digital.core.ObservableValues.ovs;
import static de.neemann.digital.core.element.PinInfo.input;

/**
 * The JK Flipflop
 *
 * @author hneemann
 */
public class FlipflopJK extends Node implements Element {

    /**
     * The JK-FF description
     */
    public static final ElementTypeDescription DESCRIPTION
            = new ElementTypeDescription("JK_FF", FlipflopJK.class, input("J"), input("C"), input("K"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.DEFAULT)
            .addAttribute(Keys.INVERTER_CONFIG)
            .addAttribute(Keys.VALUE_IS_PROBE);

    private final boolean isProbe;
    private final String label;
    private ObservableValue jVal;
    private ObservableValue kVal;
    private ObservableValue clockVal;
    private ObservableValue q;
    private ObservableValue qn;
    private boolean lastClock;
    private boolean out;

    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     */
    public FlipflopJK(ElementAttributes attributes) {
        super(true);
        this.q = new ObservableValue("Q", 1).setPinDescription(DESCRIPTION);
        this.qn = new ObservableValue("\u00ACQ", 1).setPinDescription(DESCRIPTION);
        isProbe = attributes.get(Keys.VALUE_IS_PROBE);
        label = attributes.getCleanLabel();

        int def = attributes.get(Keys.DEFAULT);
        out = def > 0;
        q.setBool(out);
        qn.setBool(!out);
    }

    /**
     * Creates a new instance
     *
     * @param label the label
     * @param q     the output
     * @param qn    the inverted output
     */
    public FlipflopJK(String label, ObservableValue q, ObservableValue qn) {
        super(true);
        this.q = q;
        this.qn = qn;
        isProbe = false;
        this.label = label;
        q.setBool(false);
        qn.setBool(true);
    }

    @Override
    public void readInputs() throws NodeException {
        boolean clock = clockVal.getBool();
        if (clock && !lastClock) {
            boolean j = jVal.getBool();
            boolean k = kVal.getBool();

            if (j && k) out = !out;
            else if (j) out = true;
            else if (k) out = false;
        }
        lastClock = clock;
    }

    @Override
    public void writeOutputs() throws NodeException {
        q.setBool(out);
        qn.setBool(!out);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws BitsException {
        jVal = inputs.get(0).checkBits(1, this, 0);
        clockVal = inputs.get(1).addObserverToValue(this).checkBits(1, this, 1);
        kVal = inputs.get(2).checkBits(1, this, 2);
    }

    /**
     * @return the j value
     */
    public ObservableValue getjVal() {
        return jVal;
    }

    /**
     * @return the k value
     */
    public ObservableValue getkVal() {
        return kVal;
    }

    /**
     * @return the clock value
     */
    public ObservableValue getClockVal() {
        return clockVal;
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    @Override
    public ObservableValues getOutputs() {
        return ovs(q, qn);
    }

    @Override
    public void registerNodes(Model model) {
        super.registerNodes(model);
        if (isProbe)
            model.addSignal(new Signal(label, q, v -> {
                out = v != 0;
                q.setBool(out);
                qn.setBool(!out);
            }));
    }

    void setOut(boolean out) {
        this.out = out;
    }
}
