package de.neemann.digital.core.wiring.bus;

import de.neemann.digital.core.BurnException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.PinDescription;

import java.util.List;

/**
 * The {@link AbstractBusHandler} calculates the state of a net with given inputs and pull resistors.
 * After the calculation of the state the method {@link AbstractBusHandler#set(long, boolean)} is called
 * to propagate the actual state.
 * Created by hneemann on 22.02.17.
 */
public abstract class AbstractBusHandler {

    private final BusModelStateObserver obs;
    private boolean burn;
    private int addedVersion = -1;

    /**
     * Creates a new instance
     *
     * @param obs the {@link BusModelStateObserver} is neede to check a burn condition.
     */
    public AbstractBusHandler(BusModelStateObserver obs) {
        this.obs = obs;
    }

    /**
     * Used to get the outputs connected to this net.
     *
     * @return the outputs connected to the net, so the inputs of the net.
     */
    public abstract List<ObservableValue> getInputs();

    /**
     * @return the resistor connected to the net, Dot return null, return {@link de.neemann.digital.core.element.PinDescription.PullResistor#none} instead.
     */
    public abstract PinDescription.PullResistor getResistor();

    /**
     * Sets the value of the net.
     *
     * @param value the value
     * @param highz the highz state
     */
    public abstract void set(long value, boolean highz);

    /**
     * recalculates the state of the net
     * Also calls {@link AbstractBusHandler#set(long, boolean)} with the new value.
     */
    public void recalculate() {
        long value = 0;
        burn = false;
        boolean highz = true;
        for (ObservableValue input : getInputs()) {
            if (!input.isHighZ()) {
                if (highz) {
                    highz = false;
                    value = input.getValue();
                } else {
                    if (value != input.getValue())
                        burn = true;
                }
            }
        }
        if (highz) {
            switch (getResistor()) {
                case pullUp:
                    set(-1, false);
                    break;
                case pullDown:
                    set(0, false);
                    break;
                default:
                    set(value, highz);
            }
        } else
            set(value, highz);

        // if burn condition and not yet added for post step check add for post step check
        if (burn && (obs.getVersion() != addedVersion)) {
            addedVersion = obs.getVersion();
            obs.addCheck(this);
        }
    }

    /**
     * Called to check if this net is in a burn condition.
     * A burn condition does not immediately throw an exception, because intermediate burn condition are
     * unavoidable. So this method is called if the step is completed. If a step ends with a burn condition
     * an exception is thrown.
     */
    public void checkBurn() {
        if (burn)
            throw new BurnException(getInputs());
    }


    /**
     * invalidates this handler.
     * indicates that this handler is not longer used
     */
    public void invalidate() {
        burn=false;
    }
}
