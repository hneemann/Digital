/*
 * Copyright (c) 2021 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.valueFormatter;

import de.neemann.digital.core.Value;

/**
 * the binary formatter
 */
public final class ValueFormatterBinary extends ValueFormatterViewEdit {
    /**
     * the singleton instance
     */
    public static final ValueFormatterBinary INSTANCE = new ValueFormatterBinary();

    private ValueFormatterBinary() {
    }

    @Override
    public int strLen(int bits) {
        return bits + 2;
    }

    @Override
    protected String format(Value inValue) {
        final int bits = inValue.getBits();
        char[] data = new char[bits];
        final long value = inValue.getValue();
        long mask = 1;
        for (int i = bits - 1; i >= 0; i--) {
            if ((value & mask) != 0)
                data[i] = '1';
            else
                data[i] = '0';
            mask <<= 1;
        }
        return "0b" + new String(data);
    }
}
