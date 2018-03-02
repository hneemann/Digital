/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.quinemc;


/**
 * A byte array.
 * Zero and one behave as expected, any other value represents "don't care"
 */
public class BoolTableByteArray implements BoolTable {

    private final byte[] table;

    /**
     * Creates a new instance
     *
     * @param rows the number of rows
     */
    public BoolTableByteArray(int rows) {
        this(new byte[rows]);
    }

    /**
     * Creates a new instance
     *
     * @param table the int values
     */
    public BoolTableByteArray(byte[] table) {
        this.table = table;
    }

    @Override
    public int size() {
        return table.length;
    }

    @Override
    public ThreeStateValue get(int i) {
        return ThreeStateValue.value(table[i]);
    }

    /**
     * Sets a table value
     *
     * @param row  the row
     * @param bool the value
     */
    public void set(int row, boolean bool) {
        set(row, bool ? 1 : 0);
    }

    /**
     * Sets a table value
     *
     * @param row   the row
     * @param value the value
     */
    public void set(int row, int value) {
        table[row] = (byte) value;
    }

    /**
     * Creates a table where all values added twive
     *
     * @param values the original values
     * @return the new values
     */
    public static BoolTableByteArray createDoubledValues(BoolTable values) {
        BoolTableByteArray t = new BoolTableByteArray(values.size() * 2);
        for (int i = 0; i < values.size(); i++) {
            int v = values.get(i).asInt();
            t.set(i * 2, v);
            t.set(i * 2 + 1, v);
        }
        return t;
    }

    /**
     * Sets the don't cares to the given value
     *
     * @param value the value
     */
    public void setXTo(int value) {
        for (int i = 0; i < table.length; i++)
            if (table[i] > 1)
                table[i] = (byte) value;
    }

    /**
     * Sets all entries to the given value
     *
     * @param value the value
     */
    public void setAllTo(int value) {
        for (int i = 0; i < table.length; i++)
            table[i] = (byte) value;
    }
}
