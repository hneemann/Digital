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
    private final ArrayList<CommonBusValue> values;
    private final ArrayList<ObservableValue> inputs;
    private final ArrayList<ObservableValue> excludes;

    /**
     * Creates a new instance
     *
     * @param obs The observer needed to check the burn condition
     */
    public ConnectedBusHandler(BusModelStateObserver obs) {
        super(obs);
        values = new ArrayList<>();
        inputs = new ArrayList<>();
        excludes = new ArrayList<>();
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

    /**
     * Adds output to the net which are to ignore.
     *
     * @param output1 output1
     * @param output2 output2
     */
    public void addExclude(ObservableValue output1, ObservableValue output2) {
        excludes.add(output1);
        excludes.add(output2);
    }

    /**
     * Adds output which are to ignore by net other.
     *
     * @param other adds the outputs to ignore from this net
     */
    public void addExcludesFrom(ConnectedBusHandler other) {
        excludes.addAll(other.excludes);
    }

    /**
     * Removes all the outputs which are to ignore
     */
    public void removeExcludes() {
        inputs.removeAll(excludes);
    }
}
