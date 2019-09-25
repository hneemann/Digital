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

    /**
     * Creates a new instance
     *
     * @param values the values to initialize the table
     */
    public BoolTableByteArray(BoolTable values) {
        table = new byte[values.size()];
        for (int i = 0; i < values.size(); i++)
            table[i] = (byte) values.get(i).asInt();
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
     * Modifies all the table elements using the given modifier.
     *
     * @param m the modifier
     */
    public void modify(TableModifier m) {
        for (int i = 0; i < table.length; i++)
            table[i] = m.modify(table[i]);
    }

    /**
     * Modifier to modify the table
     */
    public interface TableModifier {
        /**
         * Creates the modified value
         *
         * @param b the original value
         * @return the modified value
         */
        byte modify(byte b);
    }
}
