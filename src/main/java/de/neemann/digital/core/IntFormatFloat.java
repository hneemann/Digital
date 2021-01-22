/*
 * Copyright (c) 2021 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core;

/**
 * Floating point formatter
 */
public class IntFormatFloat extends IntFormat {
    /**
     * Creates a new float instance
     */
    IntFormatFloat() {
        super("float", null, bits -> 15, true);
    }

    @Override
    public String formatToView(Value inValue) {
        if (inValue.isHighZ())
            return inValue.toString();

        switch (inValue.getBits()) {
            case 32:
                return Float.toString(Float.intBitsToFloat((int) inValue.getValue()));
            case 64:
                return Double.toString(Double.longBitsToDouble(inValue.getValue()));
            default:
                return "0x" + toHex(inValue);
        }
    }

    @Override
    public String formatToEdit(Value inValue) {
        if (inValue.isHighZ())
            return "Z";

        if (inValue.getBits() == 64)
            return formatToView(inValue) + "d";
        else
            return formatToView(inValue);
    }
}
