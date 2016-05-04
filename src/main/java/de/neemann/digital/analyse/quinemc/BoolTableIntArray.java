package de.neemann.digital.analyse.quinemc;


import de.neemann.digital.analyse.expression.ExpressionException;

/**
 * A int array.
 * Zero and one behave as expected, any other value represents "don't care"
 *
 * @author hneemann
 */
public class BoolTableIntArray implements BoolTable {

    private final int[] table;

    /**
     * Creates a new instace
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
    public ThreeStateValue get(int i) throws ExpressionException {
        return ThreeStateValue.value(table[i]);
    }
}
