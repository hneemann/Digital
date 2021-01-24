/*
 * Copyright (c) 2021 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.valueFormatter;

import de.neemann.digital.core.Value;

/**
 * The default value formatter
 */
public final class ValueFormatterDefault implements ValueFormatter {
    /**
     * hex digits
     */
    public static final char[] DIGITS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    /**
     * the singleton instance
     */
    public static final ValueFormatter INSTANCE = new ValueFormatterDefault();

    private ValueFormatterDefault() {
    }

    @Override
    public String formatToView(Value inValue) {
        if (inValue.isHighZ())
            return inValue.toString();
        else
            return toShortHex(inValue.getValue(), false);
    }

    @Override
    public String formatToEdit(Value inValue) {
        if (inValue.isHighZ())
            return "Z";

        final long value = inValue.getValue();
        if (value >= 0 && value < 10)
            return Long.toString(value);
        else
            return "0x" + toShortHex(value, true);
    }

    @Override
    public int strLen(int bits) {
        return (bits - 1) / 4 + 3;
    }

    @Override
    public boolean isSuitedForAddresses() {
        return false;
    }

    /**
     * Creates a short hex representation of the given value.
     * Use only to represent a value.
     * If confusion is excluded, the prefix '0x' is omitted.
     * Thus 0x1A3 is converted to "1A3" which can not be parsed back to a long because "0x" is missing.
     *
     * @param value the value
     * @return the hex string
     */
    public static String toShortHex(long value) {
        return toShortHex(value, false);
    }

    private static final int BUF = 16;

    static String toShortHex(long value, boolean omitPrefix) {
        if (value == 0)
            return "0";

        boolean wasChar = false;
        int p = BUF;
        char[] data = new char[BUF];
        while (value != 0) {
            final int d = (int) (value & 0xf);
            if (d >= 10) wasChar = true;
            p--;
            data[p] = DIGITS[d];
            value >>>= 4;
        }

        if (omitPrefix || wasChar || p == BUF - 1)
            return new String(data, p, BUF - p);
        else
            return "0x" + new String(data, p, BUF - p);
    }

}
