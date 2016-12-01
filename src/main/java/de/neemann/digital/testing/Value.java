package de.neemann.digital.testing;

import de.neemann.digital.core.ObservableValue;

/**
 * A single value to test
 *
 * @author hneemann
 */
public class Value {

    /**
     * Types of values
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
    public Value(int val) {
        this.value = val;
        this.type = Type.NORMAL;
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
     */
    public Value(String val) {
        val = val.trim();
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
                value = Long.decode(val);
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
        if (this == v) return true;
        if (v == null) return false;

        if (v.type == Type.DONTCARE || type == Type.DONTCARE) return true;

        if (v.type != type) return false;

        // both types are equal!
        if (type == Type.HIGHZ) return true;

        return value == v.value;
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
                return ObservableValue.getHexString(value);
        }
    }

    /**
     * @return type of value
     */
    public Type getType() {
        return type;
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
        ov.set(value, isHighZ());
    }
}
