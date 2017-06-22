package de.neemann.digital.core.flipflops;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;

import static de.neemann.digital.core.ObservableValues.ovs;
import static de.neemann.digital.core.element.PinInfo.input;

/**
 * The RS Flipflop
 *
 * @author hneemann
 */
public class FlipflopRS extends Node implements Element {

    /**
     * The RS-FF description
     */
    public static final ElementTypeDescription DESCRIPTION
            = new ElementTypeDescription("RS_FF", FlipflopRS.class, input("S"), input("C"), input("R"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.DEFAULT)
            .addAttribute(Keys.INVERTER_CONFIG)
            .addAttribute(Keys.VALUE_IS_PROBE);

    private final boolean isProbe;
    private final String label;

    private ObservableValue sVal;
    private ObservableValue rVal;
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
    public FlipflopRS(ElementAttributes attributes) {
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

    @Override
    public void readInputs() throws NodeException {
        boolean clock = clockVal.getBool();
        if (clock && !lastClock) {
            boolean s = sVal.getBool();
            boolean r = rVal.getBool();

            if (s && !r) out = true;
            else if (!s && r) out = false;
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
        sVal = inputs.get(0).addObserverToValue(this).checkBits(1, this, 0);
        clockVal = inputs.get(1).addObserverToValue(this).checkBits(1, this, 1);
        rVal = inputs.get(2).addObserverToValue(this).checkBits(1, this, 2);
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
