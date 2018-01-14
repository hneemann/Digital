package de.neemann.digital.core;

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
        this.value = value & Bits.mask(bits);
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
        return Bits.signExtend(value, bits);
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
    @Override
    public String toString() {
        if (highZ)
            return "?";
        else {
            return IntFormat.toShortHex(value);
        }
    }

    /**
     * Compares two values
     *
     * @param other the other value
     * @return true if equal
     */
    public boolean isEqual(Value other) {
        if (highZ != other.highZ)
            return false;

        return highZ || value == other.value;
    }
}
