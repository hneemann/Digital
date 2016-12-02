package de.neemann.digital.testing.parser;

import de.neemann.digital.testing.Value;

import java.util.ArrayList;

/**
 * The context of the calculations
 * Created by hneemann on 02.12.16.
 */
public class Context {
    private final long n;
    private final ArrayList<Value> values;

    /**
     * Creates a new instance
     *
     * @param n      the actual loop value
     * @param values the values array to fill
     */
    public Context(long n, ArrayList<Value> values) {
        this.n = n;
        this.values = values;
    }

    /**
     * @return the actual loop value
     */
    public long getN() {
        return n;
    }

    /**
     * Adds a simple value to the value list
     *
     * @param v the value to add
     */
    public void addValue(Value v) {
        values.add(v);
    }

    /**
     * Adds bitcount values to the values list.
     * Bitcount bits from the given value are added to the values list
     *
     * @param bitCount the numbers of bits to add
     * @param value    the bit values
     */
    public void addBits(int bitCount, long value) {
        long mask = 1L << (bitCount - 1);
        for (int i = 0; i < bitCount; i++) {
            boolean v = (value & mask) != 0;
            values.add(new Value(v ? 1 : 0));
            mask >>= 1;
        }
    }
}
