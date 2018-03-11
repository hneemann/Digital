/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs.rt;

/**
 *
 * @author ideras
 */
public class IntValue extends RtValue {
    private final int value;

    /**
     * Create a new instance.
     *
     * @param value the value.
     */
    public IntValue(int value) {
        this.value = value;
    }

    /**
     * Returns the integer value.
     *
     * @return the value
     */
    public int getValue() {
        return value;
    }

    @Override
    public Type getType() {
        return Type.INT;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }


}
