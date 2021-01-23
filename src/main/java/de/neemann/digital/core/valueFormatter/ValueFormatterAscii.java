/*
 * Copyright (c) 2021 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.valueFormatter;

import de.neemann.digital.core.Value;

/**
 * The ascii formatter
 */
public final class ValueFormatterAscii extends ValueFormatterViewEdit {
    /**
     * the singleton instance
     */
    public static final ValueFormatterAscii INSTANCE = new ValueFormatterAscii();

    private ValueFormatterAscii() {
    }

    @Override
    public int strLen(int bits) {
        return 3;
    }

    @Override
    protected String format(Value value) {
        return "'" + ((char) value.getValue()) + "'";
    }
}
