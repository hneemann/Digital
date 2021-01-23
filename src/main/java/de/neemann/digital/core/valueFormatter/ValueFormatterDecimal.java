/*
 * Copyright (c) 2021 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.valueFormatter;

import de.neemann.digital.core.Value;

/**
 * The decimal value formatter
 */
public final class ValueFormatterDecimal extends ValueFormatterViewEdit {
    /**
     * the unsigned singleton instance
     */
    public static final ValueFormatterDecimal UNSIGNED = new ValueFormatterDecimal(false);
    /**
     * the signed singleton instance
     */
    public static final ValueFormatterDecimal SIGNED = new ValueFormatterDecimal(true);

    private final boolean signed;

    private ValueFormatterDecimal(boolean signed) {
        this.signed = signed;
    }

    @Override
    public int strLen(int bits) {
        if (signed)
            return decStrLen(bits - 1) + 1;
        else
            return decStrLen(bits);
    }

    static int decStrLen(int bits) {
        if (bits == 64)
            return 20;
        else if (bits == 63) {
            return 19;
        } else
            return (int) Math.ceil(Math.log10(1L << bits));
    }


    @Override
    protected String format(Value value) {
        if (signed)
            return Long.toString(value.getValueSigned());
        else
            return Long.toString(value.getValue());
    }
}
