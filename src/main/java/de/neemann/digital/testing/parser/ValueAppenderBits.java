package de.neemann.digital.testing.parser;

import de.neemann.digital.testing.Value;

import java.util.ArrayList;

/**
 * Appends the bits of an integer value to the given row.
 * Created by hneemann on 19.04.17.
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
    public void appendValues(ArrayList<Value> values, Context conext) throws ParserException {
        long value = expression.value(conext);
        long mask = 1L << (bitCount - 1);
        for (int i = 0; i < bitCount; i++) {
            boolean v = (value & mask) != 0;
            values.add(new Value(v ? 1 : 0));
            mask >>= 1;
        }
    }
}
