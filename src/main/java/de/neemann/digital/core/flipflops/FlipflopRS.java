package de.neemann.digital.core.flipflops;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;

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
            = new ElementTypeDescription("RS_FF", FlipflopRS.class, input("R"), input("C"), input("S"))
            .addAttribute(Keys.Rotate)
            .addAttribute(Keys.Label)
            .addAttribute(Keys.ValueIsProbe)
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
        this.q = new ObservableValue("Q", 1);
        this.qn = new ObservableValue("\u00ACQ", 1);
        isProbe = attributes.get(Keys.ValueIsProbe);
        label = attributes.get(Keys.Label);
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
    public void setInputs(ObservableValue... inputs) throws BitsException {
        jVal = inputs[0].addObserverToValue(this).checkBits(1, this);
        clockVal = inputs[1].addObserverToValue(this).checkBits(1, this);
        kVal = inputs[2].addObserverToValue(this).checkBits(1, this);
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
