package de.neemann.digital.analyse.quinemc;


/**
 * A simple boolean array
 *
 * @author hneemann
 */
public class BoolTableBoolArray implements BoolTable {

    private final boolean[] table;

    /**
     * Creates a new instance
     *
     * @param rows number of rows
     */
    public BoolTableBoolArray(int rows) {
        this(new boolean[rows]);
    }

    /**
     * Creates a new instance
     *
     * @param table the bool values
     */
    public BoolTableBoolArray(boolean[] table) {
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
     * Sets a value
     *
     * @param row   the row
     * @param value the value
     */
    public void set(int row, boolean value) {
        table[row] = value;
    }
}
