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
public class FlipflopJKAsync extends Node implements Element {

    /**
     * The JK-FF description
     */
    public static final ElementTypeDescription DESCRIPTION
            = new ElementTypeDescription("JK_FF_AS", FlipflopJKAsync.class,
            input("Set"),
            input("J"),
            input("C"),
            input("K"),
            input("Clr"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.DEFAULT)
            .addAttribute(Keys.INVERTERCONFIG)
            .addAttribute(Keys.VALUE_IS_PROBE);

    private final Boolean isProbe;
    private final String label;
    private ObservableValue setVal;
    private ObservableValue clrVal;
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
    public FlipflopJKAsync(ElementAttributes attributes) {
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
            boolean j = jVal.getBool();
            boolean k = kVal.getBool();

            if (j && k) out = !out;
            else if (j) out = true;
            else if (k) out = false;
        }
        lastClock = clock;

        if (setVal.getBool()) out = true;
        else if (clrVal.getBool()) out = false;
    }

    @Override
    public void writeOutputs() throws NodeException {
        q.setBool(out);
        qn.setBool(!out);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws BitsException {
        setVal = inputs.get(0).addObserverToValue(this).checkBits(1, this, 0);
        jVal = inputs.get(1).addObserverToValue(this).checkBits(1, this, 1);
        clockVal = inputs.get(2).addObserverToValue(this).checkBits(1, this, 2);
        kVal = inputs.get(3).addObserverToValue(this).checkBits(1, this, 3);
        clrVal = inputs.get(4).addObserverToValue(this).checkBits(1, this, 4);
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
