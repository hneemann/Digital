package de.neemann.digital.analyse.quinemc;


import de.neemann.digital.analyse.expression.ExpressionException;

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
    public ThreeStateValue get(int i) throws ExpressionException {
        return ThreeStateValue.value(table[i]);
    }
}
