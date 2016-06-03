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
            .addAttribute(Keys.VALUE_IS_PROBE)
            .setShortName("RS");

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
    public FlipflopRS(ElementAttributes attributes) {
        super(true);
        this.q = new ObservableValue("Q", 1);
        this.qn = new ObservableValue("\u00ACQ", 1);
        isProbe = attributes.get(Keys.VALUE_IS_PROBE);
        label = attributes.getCleanLabel();
    }

    @Override
    public void readInputs() throws NodeException {
        boolean clock = clockVal.getBool();
        if (clock && !lastClock) {
            boolean j = jVal.getBool();
            boolean k = kVal.getBool();

            if (j && !k) out = true;
            else if (!j && k) out = false;
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
        jVal = inputs.get(0).addObserverToValue(this).checkBits(1, this);
        clockVal = inputs.get(1).addObserverToValue(this).checkBits(1, this);
        kVal = inputs.get(2).addObserverToValue(this).checkBits(1, this);
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
