package de.neemann.digital.core.switching;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.draw.elements.PinException;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * N-Channel MOS FET
 * Created by hneemann on 22.02.17.
 */
public class NFET extends Node implements Element {
    /**
     * The switch description
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(NFET.class, input("G"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.BITS)
            .addAttribute(Keys.LABEL);

    private final Switch s;
    private ObservableValue input;
    private boolean closed;
    private boolean invert;

    /**
     * Create a new instance
     *
     * @param attr the attributes
     */
    public NFET(ElementAttributes attr) {
        this(attr, false);
    }

    /**
     * Create a new instance
     *
     * @param attr   the attributes
     * @param invert invert the input
     */
    protected NFET(ElementAttributes attr, boolean invert) {
        s = new Switch(attr, false);
        this.invert = invert;
        s.getOutput1().setPinDescription(DESCRIPTION);
        s.getOutput2().setPinDescription(DESCRIPTION);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        input = inputs.get(0).checkBits(1, this).addObserverToValue(this);
        s.setInputs(new ObservableValues(inputs.get(1), inputs.get(2)));

    }

    @Override
    public ObservableValues getOutputs() throws PinException {
        return s.getOutputs();
    }

    @Override
    public void readInputs() throws NodeException {
        if (input.isHighZ())
            closed = false;
        else
            closed = input.getBool() ^ invert;
    }

    @Override
    public void writeOutputs() throws NodeException {
        s.setClosed(closed);
    }

    /**
     * @return true if fet is closed
     */
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void init(Model model) throws NodeException {
        s.init(model);
    }

    /**
     * @return output 1
     */
    public ObservableValue getOutput1() {
        return s.getOutput1();
    }

    /**
     * @return output 2
     */
    public ObservableValue getOutput2() {
        return s.getOutput2();
    }

}
