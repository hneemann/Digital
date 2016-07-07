package de.neemann.digital.gui.components.test;

import de.neemann.digital.core.ObservableValue;

/**
 * A single value to test
 *
 * @author hneemann
 */
public class Value {

    private final long value;
    private final boolean highZ;
    private final boolean dontCare;

    /**
     * Creates a new value based on the given {@link ObservableValue}
     *
     * @param ov the value to copy
     */
    public Value(ObservableValue ov) {
        this.value = ov.getValue();
        this.highZ = ov.isHighZ();
        this.dontCare = false;
    }

    /**
     * Creates a new value based on a string
     *
     * @param val the string
     */
    public Value(String val) {
        val = val.trim();
        if (val.equals("X")) {
            highZ = false;
            value = 0;
            dontCare = true;
        } else {
            dontCare = false;
            if (val.equals("Z")) {
                highZ = true;
                value = 0;
            } else {
                highZ = false;
                value = Long.parseLong(val);
            }
        }
    }

    /**
     * Creates a new instance with the given value.
     * highZ and dontCare are set to false.
     *
     * @param clockValue the value
     */
    public Value(int clockValue) {
        this.value = clockValue;
        this.highZ = false;
        this.dontCare = false;
    }

    /**
     * Return true if value is equal to the given value.
     *
     * @param v the value to compare with
     * @return true if equal
     */
    public boolean isEqualTo(Value v) {
        if (this == v) return true;
        if (v == null) return false;

        if (dontCare || v.dontCare) return true;

        if (highZ && v.highZ) return true;
        if (highZ != v.highZ) return false;

        return value == v.value;
    }

    @Override
    public String toString() {
        if (dontCare)
            return "X";
        if (highZ)
            return "Z";
        return ObservableValue.getHexString(value);
    }

    /**
     * @return true if value is a dont care
     */
    public boolean isDontCare() {
        return dontCare;
    }

    /**
     * @return true if value is a high Z value
     */
    public boolean isHighZ() {
        return highZ;
    }

    /**
     * @return the value itself
     */
    public long getValue() {
        return value;
    }

    /**
     * Sets this value to a copy of the given {@link ObservableValue}
     *
     * @param ov the ObservableValue to copy
     */
    public void setTo(ObservableValue ov) {
        ov.set(value, isHighZ());
    }
}
