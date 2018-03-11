/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.wiring.bus;

import de.neemann.digital.core.NodeInterface;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.draw.elements.PinException;

import java.io.File;
import java.util.Arrays;

/**
 * This observer represents the common output value of several outputs connected together.
 * Handles also the switches.
 */
public final class CommonBusValue extends ObservableValue implements NodeInterface {
    private final BusModelStateObserver obs;
    private final PullResistor resistor;
    private final ObservableValue[] inputs;
    private final File origin;
    private AbstractBusHandler handler;

    CommonBusValue(int bits, BusModelStateObserver obs, PullResistor resistor, ObservableValue[] inputs) {
        this(bits, obs, resistor, inputs, null);
    }

    CommonBusValue(int bits, BusModelStateObserver obs, PullResistor resistor, ObservableValue[] inputs, File origin) {
        super("commonBusOut", bits);
        if (resistor.equals(PullResistor.none))
            setToHighZ();
        this.obs = obs;
        this.resistor = resistor;
        this.inputs = inputs;
        this.origin = origin;
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
        setHandler(new SingleBusHandler(obs, this, resistor, inputs).addOrigin(origin));
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

    /**
     * @return the origin of this {@link CommonBusValue}
     */
    public File getOrigin() {
        return origin;
    }
}
