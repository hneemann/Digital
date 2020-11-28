/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.testing.parser;

import de.neemann.digital.data.Value;

import java.util.ArrayList;

/**
 * Appends the bits of an integer value to the given row.
 */
public class ValueAppenderBits implements ValueAppender {
    private final Expression expression;
    private final int bitCount;

    /**
     * Creates a new instance
     *
     * @param bitCount   the number of bits to append
     * @param expression the expression to calculate the int value
     */
    public ValueAppenderBits(int bitCount, Expression expression) {
        this.bitCount = bitCount;
        this.expression = expression;
    }

    @Override
    public void appendValues(ArrayList<Value> values, Context context) throws ParserException {
        long value = expression.value(context);
        long mask = 1L << (bitCount - 1);
        for (int i = 0; i < bitCount; i++) {
            boolean v = (value & mask) != 0;
            values.add(new Value(v ? 1 : 0));
            mask >>>= 1;
        }
    }
}
