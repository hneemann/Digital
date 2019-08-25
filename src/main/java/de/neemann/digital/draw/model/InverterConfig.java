/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.model;

import de.neemann.digital.core.NodeWithoutDelay;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.hdl.hgs.HGSMap;

import java.util.HashSet;
import java.util.Objects;

/**
 * Manages the input inverting of a component
 */
public final class InverterConfig implements HGSMap {

    private HashSet<String> inputs;

    private InverterConfig(HashSet<String> inputs) {
        this.inputs = inputs;
    }

    /**
     * Handles the inverting of a input signal
     * if the given input is not to invert, the original input is returned,
     * If the input is to invert, a inverted input is returned. This invert does not add
     * a additional delay time.
     *
     * @param name the name of the signal
     * @param orig the original input signal
     * @return the inverted or the original input
     */
    public ObservableValue invert(String name, ObservableValue orig) {
        if (inputs == null)
            return orig;

        if (!inputs.contains(name))
            return orig;

        ObservableValue out = new ObservableValue("~" + orig.getName(), orig.getBits());
        orig.addObserver(new NodeWithoutDelay(out) {
            @Override
            public void hasChanged() {
                out.set(~orig.getValue(), orig.getHighZ());
            }
        });
        out.set(~orig.getValue(), orig.getHighZ());
        return out;
    }

    /**
     * @return the string representation of the inverter config
     */
    public String toString() {
        return inputs.toString();
    }

    /**
     * Returns true if the input with the given name is to invert.
     *
     * @param name the name of the signal
     * @return true if the given input is to invert.
     */
    public boolean contains(String name) {
        if (inputs == null)
            return false;

        return inputs.contains(name);
    }

    /**
     * @return true if no signal is to invert
     */
    public boolean isEmpty() {
        if (inputs == null)
            return true;

        return inputs.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InverterConfig that = (InverterConfig) o;

        return Objects.equals(inputs, that.inputs);
    }

    @Override
    public int hashCode() {
        return inputs != null ? inputs.hashCode() : 0;
    }

    /**
     * @return the number of inverted inputs
     */
    public int size() {
        if (inputs == null)
            return 0;
        return inputs.size();
    }

    @Override
    public Object hgsMapGet(String key) {
        if (inputs == null)
            return false;

        return inputs.contains(key);
    }

    /**
     * Builder to create InverterConfig instances
     */
    public static class Builder {

        private HashSet<String> inputs;

        /**
         * Adds a signal to invert
         *
         * @param name the signale
         * @return this for chained calls
         */
        public Builder add(String name) {
            if (inputs == null)
                inputs = new HashSet<>();
            inputs.add(name);
            return this;
        }

        /**
         * Creats the instance
         *
         * @return the created instance
         */
        public InverterConfig build() {
            return new InverterConfig(inputs);
        }

    }
}
