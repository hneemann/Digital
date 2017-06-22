package de.neemann.digital.core.flipflops;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.*;

import static de.neemann.digital.core.ObservableValues.ovs;
import static de.neemann.digital.core.element.PinInfo.input;

/**
 * The T Flipflop
 *
 * @author hneemann
 */
public class FlipflopT extends Node implements Element {

    /**
     * The T-FF description
     */
    public static final ElementTypeDescription DESCRIPTION
            = new ElementTypeDescription("T_FF", FlipflopT.class) {
        @Override
        public PinDescriptions getInputDescription(ElementAttributes elementAttributes) throws NodeException {
            if (elementAttributes.get(Keys.WITH_ENABLE))
                return new PinDescriptions(input("T"), input("C")).setLangKey(getPinLangKey());
            else
                return new PinDescriptions(input("C")).setLangKey(getPinLangKey());
        }
    }
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.WITH_ENABLE)
            .addAttribute(Keys.DEFAULT)
            .addAttribute(Keys.INVERTER_CONFIG)
            .addAttribute(Keys.VALUE_IS_PROBE);

    private final boolean isProbe;
    private final String label;
    private final boolean isEnable;

    private ObservableValue clockVal;
    private ObservableValue enable;
    private ObservableValue q;
    private ObservableValue qn;
    private boolean lastClock;
    private boolean out;

    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     */
    public FlipflopT(ElementAttributes attributes) {
        super(true);
        this.q = new ObservableValue("Q", 1).setPinDescription(DESCRIPTION);
        this.qn = new ObservableValue("\u00ACQ", 1).setPinDescription(DESCRIPTION);
        isEnable = attributes.get(Keys.WITH_ENABLE);
        isProbe = attributes.get(Keys.VALUE_IS_PROBE);
        label = attributes.getCleanLabel();

        int def = attributes.get(Keys.DEFAULT);
        out = def > 0;
        q.setBool(out);
        qn.setBool(!out);
    }

    @Override
    public void readInputs() throws NodeException {
        boolean clock = clockVal.getBool();
        if (clock && !lastClock) {
            if (enable == null)
                out = !out;
            else {
                if (enable.getBool())
                    out = !out;
            }
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
        if (isEnable) {
            enable = inputs.get(0).addObserverToValue(this).checkBits(1, this, 0);
            clockVal = inputs.get(1).addObserverToValue(this).checkBits(1, this, 1);
        } else
            clockVal = inputs.get(0).addObserverToValue(this).checkBits(1, this, 0);
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

    /**
     * @return the clock value
     */
    public ObservableValue getClockVal() {
        return clockVal;
    }

    /**
     * @return enable value or null if not available
     */
    public ObservableValue getEnableVal() {
        return enable;
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }
}
