/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.data;

import de.neemann.digital.core.Bits;
import de.neemann.digital.core.IntFormat;
import de.neemann.digital.core.ObservableValue;

/**
 * A single value to test
 */
public class Value {

    /**
     * Types of value
     */
    public enum Type {
        /**
         * normal value, value is stored in member variable value
         */
        NORMAL,
        /**
         * Value don't care in test
         */
        DONTCARE,
        /**
         * value is "high impedance"
         */
        HIGHZ,
        /**
         * its a clock value which is handled as a 0-1-0 sequence
         */
        CLOCK
    }

    /**
     * state of value
     */
    public enum State {
        /**
         * a normals value
         */
        NORMAL,
        /**
         * value is a passed test
         */
        PASS,
        /**
         * value is a failed test
         */
        FAIL

    }

    /**
     * @return returns a high z value
     */
    public static Value getHighZ() {
        return new Value(0, Type.HIGHZ);
    }

    private final long value;
    private final Type type;

    /**
     * Copy constructor
     *
     * @param value value to copy
     */
    public Value(Value value) {
        this.value = value.value;
        this.type = value.type;
    }

    /**
     * Create a simple int value
     *
     * @param val the value
     */
    public Value(long val) {
        this.value = val;
        this.type = Type.NORMAL;
    }

    private Value(long val, Type type) {
        this.value = val;
        this.type = type;
    }

    /**
     * Creates a new value based on the given {@link ObservableValue}
     *
     * @param ov the value to copy
     */
    public Value(ObservableValue ov) {
        this.value = ov.getValue();
        if (ov.isHighZ())
            this.type = Type.HIGHZ;
        else
            this.type = Type.NORMAL;
    }

    /**
     * Creates a new value based on a string
     *
     * @param val the string
     * @throws Bits.NumberFormatException Bits.NumberFormatException
     */
    public Value(String val) throws Bits.NumberFormatException {
        val = val.trim().toUpperCase();
        switch (val) {
            case "X":
                value = 0;
                type = Type.DONTCARE;
                break;
            case "Z":
                value = 0;
                type = Type.HIGHZ;
                break;
            case "C":
                value = 1;
                type = Type.CLOCK;
                break;
            default:
                value = Bits.decode(val);
                type = Type.NORMAL;
                break;
        }
    }

    /**
     * Return true if value is equal to the given value.
     *
     * @param v the value to compare with
     * @return true if equal
     */
    public boolean isEqualTo(Value v) {
        return isEqualTo(v, 0);
    }

    /**
     * Return true if value is equal to the given value.
     * Only the bits which are set in the mask are compared.
     *
     * @param v    the value to compare with
     * @param mask the mask with bits to take into account
     * @return true if equal
     */
    protected boolean isEqualTo(Value v, long mask) {
        if (this == v) return true;
        if (v == null) return false;

        if (v.type == Type.DONTCARE || type == Type.DONTCARE) return true;

        if (v.type != type) return false;

        // both types are equal!
        if (type == Type.HIGHZ) return true;

        if (mask == 0)
            return value == v.value;
        else
            return (value & mask) == (v.value & mask);
    }

    @Override
    public String toString() {
        switch (type) {
            case HIGHZ:
                return "Z";
            case DONTCARE:
                return "X";
            case CLOCK:
                return "C";
            default:
                return IntFormat.toShortHex(value);
        }
    }

    /**
     * @return type of value
     */
    public Type getType() {
        return type;
    }

    /**
     * @return the state of this value
     */
    public State getState() {
        return State.NORMAL;
    }

    /**
     * @return true if value is a high Z value
     */
    public boolean isHighZ() {
        return type == Type.HIGHZ;
    }

    /**
     * @return the value itself
     */
    public long getValue() {
        return value;
    }

    /**
     * Sets this value to the given {@link ObservableValue}
     *
     * @param ov the ObservableValue to update
     */
    public void copyTo(ObservableValue ov) {
        if (isHighZ())
            ov.setToHighZ();
        else
            ov.setValue(value);
    }
}
