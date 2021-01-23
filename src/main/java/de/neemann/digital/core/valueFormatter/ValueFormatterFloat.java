/*
 * Copyright (c) 2021 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.valueFormatter;

import de.neemann.digital.core.Value;

/**
 * Floating point formatter
 */
public final class ValueFormatterFloat implements ValueFormatter {
    private static final int SIZE32 = Float.toString((float) -Math.PI).length();
    private static final int SIZE64 = Double.toString(-Math.PI).length();

    /**
     * the singleton instance
     */
    public static final ValueFormatterFloat INSTANCE = new ValueFormatterFloat();

    private ValueFormatterFloat() {
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
                return ValueFormatterHex.INSTANCE.formatToView(inValue);
        }
    }

    @Override
    public String formatToEdit(Value inValue) {
        if (inValue.isHighZ())
            return "Z";

        switch (inValue.getBits()) {
            case 32:
                return Float.toString(Float.intBitsToFloat((int) inValue.getValue()));
            case 64:
                return Double.toString(Double.longBitsToDouble(inValue.getValue())) + "d";
            default:
                return ValueFormatterHex.INSTANCE.formatToEdit(inValue);
        }
    }

    @Override
    public int strLen(int bits) {
        switch (bits) {
            case 32:
                return SIZE32;
            case 64:
                return SIZE64;
            default:
                return ValueFormatterHex.INSTANCE.strLen(bits);
        }
    }

}
