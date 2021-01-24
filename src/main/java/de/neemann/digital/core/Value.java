/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core;

import de.neemann.digital.core.io.InValue;

import static de.neemann.digital.core.ObservableValue.zMaskString;

/**
 * Represents a copy of a value.
 * Call {@link ObservableValue#getCopy()} to obtain a value.
 */
public class Value {
    private final long value;
    private final long highZ;
    private final int bits;
    private final long mask;

    /**
     * Creates a new high z Value
     *
     * @param bits the number of high z bits
     */
    public Value(int bits) {
        this.bits = bits;
        this.mask = Bits.mask(bits);
        this.value = 0;
        this.highZ = mask;
    }

    /**
     * Creates a new Value
     *
     * @param value the value
     * @param bits  the number of bits
     */
    public Value(long value, int bits) {
        this.bits = bits;
        this.mask = Bits.mask(bits);
        this.value = value & mask;
        this.highZ = 0;
    }

    /**
     * Creates a new Value
     *
     * @param value the value
     * @param bits  the number of bits
     */
    public Value(InValue value, int bits) {
        this.bits = bits;
        this.mask = Bits.mask(bits);
        if (value.isHighZ()) {
            this.value = 0;
            this.highZ = mask;
        } else {
            this.value = value.getValue() & mask;
            this.highZ = 0;
        }
    }

    Value(ObservableValue observableValue) {
        value = observableValue.getValue();
        highZ = observableValue.getHighZ();
        bits = observableValue.getBits();
        this.mask = Bits.mask(bits);
    }

    /**
     * @return true if one of the bits is in high z state
     */
    public boolean isHighZ() {
        return highZ != 0;
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
        if (highZ != 0)
            if (highZ == mask)
                return "Z";
            else {
                return zMaskString(value, highZ, bits);
            }
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

        long m = ~highZ;

        return (value & m) == (other.value & m);
    }

    /**
     * Applies the value to the oObservable
     *
     * @param val the observable value
     */
    public void applyTo(ObservableValue val) {
        val.set(value, highZ);
    }
}
