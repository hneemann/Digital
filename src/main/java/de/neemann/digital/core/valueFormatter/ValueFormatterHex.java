/*
 * Copyright (c) 2021 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.valueFormatter;

import de.neemann.digital.core.Value;

/**
 * the hexadecimal formatter
 */
public final class ValueFormatterHex extends ValueFormatterViewEdit {
    /**
     * the singleton instance
     */
    public static final ValueFormatterHex INSTANCE = new ValueFormatterHex();

    private ValueFormatterHex() {
    }

    @Override
    protected String format(Value inValue) {
        final int bits = inValue.getBits();
        final int numChars = (bits - 1) / 4 + 1;

        StringBuilder sb = new StringBuilder("0x");
        final long value = inValue.getValue();
        for (int i = numChars - 1; i >= 0; i--) {
            int c = (int) ((value >> (i * 4)) & 0xf);
            sb.append(ValueFormatterDefault.DIGITS[c]);
        }
        return sb.toString();
    }

    @Override
    public int strLen(int bits) {
        return (bits - 1) / 4 + 3;
    }

}
