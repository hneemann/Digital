package de.neemann.digital.core;

import static de.neemann.digital.core.ObservableValue.getHexString;

/**
 * Represents a copy of a value.
 * Call {@link ObservableValue#getCopy()} to obtain a value.
 */
public class Value {
    private final long value;
    private final boolean highZ;
    private final int bits;

    /**
     * Creates a new Value
     *
     * @param value the value
     * @param bits  the number of bits
     */
    public Value(long value, int bits) {
        this.bits = bits;
        if (bits >= 64)
            this.value = value;
        else
            this.value = value & ((1L << bits) - 1);
        this.highZ = false;
    }

    Value(ObservableValue observableValue) {
        value = observableValue.getValue();
        highZ = observableValue.isHighZ();
        bits = observableValue.getBits();
    }

    /**
     * @return true if value is in high z state
     */
    public boolean isHighZ() {
        return highZ;
    }

    /**
     * @return the signals value
     */
    public long getValue() {
        return value;
    }

    /**
     * @return the signals value
     */
    public long getValueSigned() {
        if (bits >= 64)
            return value;
        else {
            if ((value & (1L << (bits - 1))) == 0)
                return value;
            else
                return value | ~((1L << bits) - 1);
        }
    }

    /**
     * @return a bool value
     */
    public boolean getBool() {
        return value != 0;
    }

    /**
     * @return the number of bits
     */
    public int getBits() {
        return bits;
    }

    /**
     * returns the actual value as a string
     *
     * @return the value as string
     */
    public String getValueString() {
        if (highZ)
            return "?";
        else {
            return getHexString(value);
        }
    }

}
