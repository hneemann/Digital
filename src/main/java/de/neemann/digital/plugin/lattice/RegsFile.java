package de.neemann.digital.plugin.lattice;

import de.neemann.digital.core.Node;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * Define the RegsFile component
 */
public class RegsFile extends Node implements Element {

    /**
     * The RegsFile description
     */
    public final static ElementTypeDescription DESCRIPTION = new ElementTypeDescription(RegsFile.class,
            input("D"),
            input("C").setClock(),
            input("en")
    )
            .addAttribute(Keys.BITS)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.INVERTER_CONFIG)
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.IS_PROGRAM_COUNTER)
            .addAttribute(Keys.VALUE_IS_PROBE);

    private final int bits;
    private final boolean isProbe;
    private final String label;
    private final boolean isProgramCounter;
    private ObservableValue dVal;
    private ObservableValue clockVal;
    private ObservableValue enableVal;

    private ObservableValue q;

    private boolean lastClock;
    private long value;

    /**
     * Creates a new instance
     *
     * @param attr the attributes
     */
    public RegsFile(ElementAttributes attr) {
        super(true);
        bits = attr.get(Keys.BITS);
        isProbe = attr.get(Keys.VALUE_IS_PROBE);
        label = attr.get(Keys.LABEL);
        isProgramCounter = attr.get(Keys.IS_PROGRAM_COUNTER);

        q = new ObservableValue("Q", bits).setPinDescription(DESCRIPTION);
    }

    @Override
    public void readInputs() throws NodeException {
        boolean enable = enableVal.getBool();
        boolean clock = clockVal.getBool();
        if (clock && !lastClock && enable)
            value = dVal.getValue();
        if (!clock && lastClock && enable)
            value = 0;
        lastClock = clock;
    }

    @Override
    public void writeOutputs() throws NodeException {
        q.setValue(value);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        dVal = inputs.get(0).checkBits(bits, this);
        clockVal = inputs.get(1).addObserverToValue(this).checkBits(1, this);
        enableVal = inputs.get(2).checkBits(1, this);
    }

    @Override
    public ObservableValues getOutputs() {
        return q.asList();
    }
}
