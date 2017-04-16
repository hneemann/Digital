package de.neemann.digital.core.switching;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.draw.elements.PinException;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * A simple relay.
 * Created by hneemann on 22.02.17.
 */
public class Relay extends Node implements Element {

    /**
     * The switch description
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(Relay.class, input("in1"), input("in2"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.BITS)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.RELAY_NORMALLY_CLOSED);


    private final boolean invers;
    private final Switch s;
    private ObservableValue input1;
    private ObservableValue input2;
    private boolean state;
    private boolean stateHighZ;

    /**
     * Create a new instance
     *
     * @param attr the attributes
     */
    public Relay(ElementAttributes attr) {
        this(attr, attr.get(Keys.RELAY_NORMALLY_CLOSED));
    }

    /**
     * Create a new instance
     *
     * @param attr   the attributes
     * @param invers true if relay is closed on zero in.
     */
    public Relay(ElementAttributes attr, boolean invers) {
        this.invers = invers;
        s = new Switch(attr, invers, "out1", "out2");
    }

    @Override
    public ObservableValues getOutputs() throws PinException {
        return s.getOutputs();
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        input1 = inputs.get(0).checkBits(1, this).addObserverToValue(this);
        input2 = inputs.get(1).checkBits(1, this).addObserverToValue(this);
        s.setInputs(new ObservableValues(inputs.get(2), inputs.get(3)));
    }

    @Override
    public void readInputs() throws NodeException {
        state = input1.getBool() ^ input2.getBool();
        stateHighZ = input1.isHighZ() || input2.isHighZ();
    }

    @Override
    public void writeOutputs() throws NodeException {
        if (stateHighZ)
            s.setClosed(invers);
        else
            s.setClosed(state ^ invers);
    }

    @Override
    public void init(Model model) throws NodeException {
        s.init(model);
    }

    /**
     * @return true if closed
     */
    public boolean isClosed() {
        return s.isClosed();
    }
}
