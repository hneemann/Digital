/*
 * Copyright (c) 2021 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.valueFormatter;

import de.neemann.digital.core.Value;

import static de.neemann.digital.core.valueFormatter.ValueFormatterDefault.DIGITS;

/**
 * the octal formatter
 */
public final class ValueFormatterOctal extends ValueFormatterViewEdit {
    /**
     * the singleton instance
     */
    public static final ValueFormatterOctal INSTANCE = new ValueFormatterOctal();

    private ValueFormatterOctal() {
    }

    @Override
    public int strLen(int bits) {
        return (bits - 1) / 3 + 3;
    }

    @Override
    protected String format(Value inValue) {
        final int bits = inValue.getBits();
        final int numChars = (bits - 1) / 3 + 1;

        StringBuilder sb = new StringBuilder("0");
        final long value = inValue.getValue();
        for (int i = numChars - 1; i >= 0; i--) {
            int c = (int) ((value >> (i * 3)) & 0x7);
            sb.append(DIGITS[c]);
        }
        return sb.toString();
    }
}
