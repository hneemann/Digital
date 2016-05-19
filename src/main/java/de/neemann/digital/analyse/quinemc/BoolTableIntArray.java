package de.neemann.digital.analyse.quinemc;


/**
 * A int array.
 * Zero and one behave as expected, any other value represents "don't care"
 *
 * @author hneemann
 */
public class BoolTableIntArray implements BoolTable {

    private final int[] table;

    /**
     * Creates a new instance
     *
     * @param rows the number of rows
     */
    public BoolTableIntArray(int rows) {
        this(new int[rows]);
    }

    /**
     * Creates a new instance
     *
     * @param table the int values
     */
    public BoolTableIntArray(int[] table) {
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
        table[row] = value;
    }

    /**
     * Creates a table where all values added twive
     *
     * @param values the original values
     * @return the new values
     */
    public static BoolTableIntArray createDoubledValues(BoolTable values) {
        BoolTableIntArray t = new BoolTableIntArray(values.size() * 2);
        for (int i = 0; i < values.size(); i++) {
            int v = values.get(i).asInt();
            t.set(i * 2, v);
            t.set(i * 2 + 1, v);
        }
        return t;
    }
}
