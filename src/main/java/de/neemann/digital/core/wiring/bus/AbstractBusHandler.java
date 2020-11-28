/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.wiring.bus;

import de.neemann.digital.core.BurnException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.element.PinDescription;
import de.neemann.digital.lang.Lang;

import java.io.File;
import java.util.HashSet;
import java.util.List;

/**
 * The {@link AbstractBusHandler} calculates the state of a net with given inputs and pull resistors.
 * After the calculation of the state the method {@link AbstractBusHandler#set(long, long)} is called
 * to propagate the actual state.
 */
public abstract class AbstractBusHandler {

    private final BusModelStateObserver obs;
    private HashSet<File> origin;

    private enum State {ok, burn, both}

    private State burn;
    private int addedVersion = -1;

    /**
     * Creates a new instance
     *
     * @param obs the {@link BusModelStateObserver} is neede to check a burn condition.
     */
    AbstractBusHandler(BusModelStateObserver obs) {
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
    public abstract void set(long value, long highz);

    /**
     * Returns all connected observable values
     * Used to create appropriate error messages.
     *
     * @return all connected observable values
     */
    public abstract ObservableValues getAllValues();

    /**
     * recalculates the state of the net
     * Also calls {@link AbstractBusHandler#set(long, long)} with the new value.
     */
    void recalculate() {
        long value = 0;
        burn = State.ok;
        if (getResistor().equals(PinDescription.PullResistor.both)) {
            burn = State.both;
            set(0, -1);
        } else {
            long highz = -1;
            for (ObservableValue input : getInputs()) {
                highz &= input.getHighZ();
                value |= input.getValueHighZIsZero();
            }

            // check for a burn condition!
            for (ObservableValue input : getInputs()) {
                long bothDefine = ~(highz | input.getHighZ());
                if ((value & bothDefine) != (input.getValueHighZIsZero() & bothDefine)) {
                    burn = State.burn;
                    break;
                }
            }

            switch (getResistor()) {
                case pullUp:
                    set(value | highz, 0);
                    break;
                case pullDown:
                    set(value, 0);
                    break;
                default:
                    set(value, highz);
            }
        }

        // if burn condition and not yet added for post step check add for post step check
        if (burn != State.ok && (obs.getVersion() != addedVersion)) {
            addedVersion = obs.getVersion();
            obs.addCheck(this);
        }
    }

    /**
     * Called to check if this net is in a burn condition.
     * A burn condition does not immediately throw an exception, because intermediate burn conditions are
     * unavoidable. So this method is called if the step is completed. If a step ends with a burn condition
     * an exception is thrown.
     */
    void checkBurn() {
        switch (burn) {
            case burn:
                throw new BurnException(Lang.get("err_burnError"), getAllValues()).addOrigin(origin);
            case both:
                throw new BurnException(Lang.get("err_pullUpAndDown"), getAllValues()).addOrigin(origin);
        }
    }

    /**
     * Sets the origin of this AbstractBusHandler
     *
     * @param file the origin
     * @return this for chained calls
     */
    AbstractBusHandler addOrigin(File file) {
        if (origin == null)
            origin = new HashSet<>();
        origin.add(file);
        return this;
    }
}
