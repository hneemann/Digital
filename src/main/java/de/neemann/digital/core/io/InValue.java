/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.io;

import de.neemann.digital.core.Bits;
import de.neemann.digital.core.ObservableValue;

/**
 * A simple value.
 * Used to store a default value in the attributes.
 */
public class InValue {

    private final long value;
    private final boolean highZ;

    /**
     * Creates a new value
     *
     * @param value the value
     */
    public InValue(long value) {
        this.value = value;
        this.highZ = false;
    }

    /**
     * Creates a new value
     *
     * @param value the value
     */
    public InValue(ObservableValue value) {
        if (value.isHighZ()) {
            this.highZ = true;
            this.value = 0;
        } else {
            this.highZ = false;
            this.value = value.getValue();
        }
    }

    /**
     * Creates a new instance
     *
     * @param value the value a "Z" means "high z"
     * @throws Bits.NumberFormatException NumberFormatException
     */
    public InValue(String value) throws Bits.NumberFormatException {
        if (value.trim().equalsIgnoreCase("z")) {
            this.highZ = true;
            this.value = 0;
        } else {
            this.highZ = false;
            this.value = Bits.decode(value.trim());
        }

    }

    /**
     * @return the value
     */
    public long getValue() {
        return value;
    }

    /**
     * @return High Z State
     */
    public boolean isHighZ() {
        return highZ;
    }

    @Override
    public String toString() {
        if (highZ)
            return "Z";
        else
            return Long.toString(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InValue inValue = (InValue) o;

        if (value != inValue.value) return false;
        return highZ == inValue.highZ;
    }

    @Override
    public int hashCode() {
        int result = (int) (value ^ (value >>> 32));
        result = 31 * result + (highZ ? 1 : 0);
        return result;
    }
}
