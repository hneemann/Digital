package de.neemann.digital.core.wiring.bus;

import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.Observer;

/**
 * This observer is added to all outputs connected together
 */
public final class CommonBusValue extends ObservableValue implements Observer {
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
    public void setHandler(AbstractBusHandler handler) {
        this.handler = handler;
    }

    /**
     * Resets the handler. SO this net is isolated to a single simple net.
     */
    public void resetHandler() {
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
}
