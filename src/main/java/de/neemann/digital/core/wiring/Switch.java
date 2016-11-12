package de.neemann.digital.core.wiring;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.*;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * A simple switch
 */
public class Switch implements Element, Observer {

    /**
     * The diodes description
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(Switch.class, input("in"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.BITS)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.CLOSED);

    private final ObservableValue output;
    private final int bits;
    private boolean closed;
    private ObservableValue input;

    /**
     * Creates a new instance
     *
     * @param attr the elements attributes
     */
    public Switch(ElementAttributes attr) {
        bits = attr.getBits();
        closed = attr.get(Keys.CLOSED);
        output = new ObservableValue("out", bits, true);
        if (!closed)
            output.set(0, true);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        input = inputs.get(0).addObserverToValue(this).checkBits(bits, null);
    }

    @Override
    public ObservableValues getOutputs() {
        return output.asList();
    }

    @Override
    public void registerNodes(Model model) {
        // its just a wire and has no delay, so it is'nt a node
    }

    @Override
    public void hasChanged() {
        if (closed) {
            output.set(input.getValue(), input.isHighZ());
        } else {
            output.set(0, true);
        }
    }

    @Override
    public void init(Model model) throws NodeException {
        hasChanged();
    }

    /**
     * Sets the closed state of the switch
     *
     * @param closed true if closed
     */
    public void setClosed(boolean closed) {
        if (this.closed != closed) {
            this.closed = closed;
            hasChanged();
        }
    }
}
