package de.neemann.digital.core.wiring.bus;

import de.neemann.digital.core.NodeInterface;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.draw.elements.PinException;

import java.util.Arrays;

/**
 * This observer is added to all outputs connected together
 */
public final class CommonBusValue extends ObservableValue implements NodeInterface {
    private final BusModelStateObserver obs;
    private final PullResistor resistor;
    private final ObservableValue[] inputs;
    private AbstractBusHandler handler;

    CommonBusValue(int bits, BusModelStateObserver obs, PullResistor resistor, ObservableValue[] inputs) {
        super("commonBusOut", bits, resistor.equals(PullResistor.none));
        this.obs = obs;
        this.resistor = resistor;
        this.inputs = inputs;
        resetHandler();
    }

    @Override
    public void hasChanged() {
        handler.recalculate();
    }

    /**
     * Sets the handler which is needed to calculate the nets state
     *
     * @param handler the handler
     */
    void setHandler(AbstractBusHandler handler) {
        this.handler = handler;
    }

    /**
     * Resets the handler. So this net is isolated to a single simple net.
     */
    void resetHandler() {
        setHandler(new SingleBusHandler(obs, this, resistor, inputs));
        hasChanged();
    }

    /**
     * @return the pull resistor is this net
     */
    public PullResistor getResistor() {
        return resistor;
    }

    /**
     * @return the inputs connected to this net.
     */
    public ObservableValue[] getInputs() {
        return inputs;
    }

    /**
     * Returns true if this net is a constant
     *
     * @return the constant if this is a constant, null otherwise
     */
    public ObservableValue searchConstant() {
        for (ObservableValue i : inputs)
            if (i.isConstant())
                return i;
        return null;
    }

    /**
     * Checks if this net is always defined.
     * This means it can never be in a high z state.
     *
     * @return true if this net is always defined
     */
    public boolean isAlwaysDefined() {
        for (ObservableValue i : inputs)
            if (!i.supportsHighZ())
                return true;
        return false;
    }

    @Override
    public String toString() {
        return "CommonBusValue{"
                + "inputs=" + Arrays.toString(inputs)
                + "', -->" + super.toString() + " }";
    }

    @Override
    public ObservableValues getOutputs() throws PinException {
        return new ObservableValues(this);
    }
}
