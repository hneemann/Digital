package de.neemann.digital.core.wiring;

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
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(Relay.class, input("in"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.BITS)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.RELAY_NORMALLY_CLOSED);


    private final boolean invers;
    private final Switch s;
    private ObservableValue input;
    private boolean state;
    private boolean stateHighZ;
    private ObservableValue in1;
    private ObservableValue in2;

    /**
     * Create a new instance
     *
     * @param attr the attributes
     */
    public Relay(ElementAttributes attr) {
        invers = attr.get(Keys.RELAY_NORMALLY_CLOSED);
        s = new Switch(attr, invers);
    }

    @Override
    public ObservableValues getOutputs() throws PinException {
        return s.getOutputs();
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        input = inputs.get(0).checkBits(1, this).addObserverToValue(this);
        in1 = inputs.get(1);
        in2 = inputs.get(2);
        s.setInputs(new ObservableValues(in1, in2));
    }

    @Override
    public void readInputs() throws NodeException {
        state = input.getBool();
        stateHighZ = input.isHighZ();
    }

    @Override
    public void writeOutputs() throws NodeException {
        s.setClosed(getClosed(state, stateHighZ, in1, in2));
    }

    /**
     * get the closed state
     *
     * @param inState the input state
     * @param inHighZ input high z value
     * @param in1 input 1
     * @param in2 input 2
     * @return true if switch is to close
     */
    protected boolean getClosed(boolean inState, boolean inHighZ, ObservableValue in1, ObservableValue in2) {
        if (inHighZ)
            return invers;

        return inState ^ invers;
    }

    @Override
    public void init(Model model) throws NodeException {
        s.init(model);
    }

    /**
     * @return output 1
     */
    protected ObservableValue getOutput1() {
        return s.getOutput1();
    }

    /**
     * @return output 2
     */
    protected ObservableValue getOutput2() {
        return s.getOutput2();
    }

    /**
     * @return true is switch is closed
     */
    public boolean isClosed() {
        return s.isClosed();
    }
}
