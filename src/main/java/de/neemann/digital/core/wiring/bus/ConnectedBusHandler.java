/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.wiring.bus;

import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.element.PinDescription;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The ConnectedBusHandler calculates the state of net, which is formed by the single nets
 * connected by a closed switch.
 */
public final class ConnectedBusHandler extends AbstractBusHandler {
    private PinDescription.PullResistor resistor = PinDescription.PullResistor.none;
    private ArrayList<CommonBusValue> values;
    private ArrayList<ObservableValue> inputs;

    /**
     * Creates a new instance
     *
     * @param obs The observer needed to check the burn condition
     */
    public ConnectedBusHandler(BusModelStateObserver obs) {
        super(obs);
        values = new ArrayList<>();
        inputs = new ArrayList<>();
    }

    /**
     * Adds a commonBusValue to the common unified commonBusValue
     *
     * @param commonBusValue the commonBusValue to add
     */
    public void addNet(CommonBusValue commonBusValue) {
        values.add(commonBusValue);
        inputs.addAll(Arrays.asList(commonBusValue.getInputs()));

        if (!commonBusValue.getResistor().equals(PinDescription.PullResistor.none)) {
            if (resistor.equals(PinDescription.PullResistor.none)) {
                resistor = commonBusValue.getResistor();
            } else {
                if (!resistor.equals(commonBusValue.getResistor())) {
                    // set error condition
                    resistor = PinDescription.PullResistor.both;
                }
            }
        }

        commonBusValue.setHandler(this);
        addOrigin(commonBusValue.getOrigin());
    }

    /**
     * Adds all nets in the given {@link ConnectedBusHandler}.
     *
     * @param h2 the {@link ConnectedBusHandler}
     */
    public void addNet(ConnectedBusHandler h2) {
        for (CommonBusValue cbv : h2.values)
            addNet(cbv);
    }

    @Override
    public List<ObservableValue> getInputs() {
        return inputs;
    }

    @Override
    public PinDescription.PullResistor getResistor() {
        return resistor;
    }

    @Override
    public void set(long value, long highz) {
        for (ObservableValue val : values)
            val.set(value, highz);
    }

    @Override
    public ObservableValues getAllValues() {
        return new ObservableValues.Builder().add(getInputs()).add(values).build();
    }

    /**
     * @return all the nets connected by this handler.
     */
    public ArrayList<CommonBusValue> getValues() {
        return values;
    }

    @Override
    public String toString() {
        return "ConnectedBusHandler{"
                + "values=" + values + '}';
    }
}
