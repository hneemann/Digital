package de.neemann.digital.data;

import java.util.ArrayList;

/**
 * Stores values in a table
 * Created by hneemann on 03.07.17.
 */
public class ValueTable {

    private final ArrayList<String> names;
    private final ArrayList<Value[]> values;

    /**
     * Creates a new table.
     *
     * @param names the cignal names
     */
    public ValueTable(ArrayList<String> names) {
        this.names = names;
        values = new ArrayList<>();
    }

    /**
     * @return number of rows
     */
    public int getRows() {
        return values.size();
    }

    /**
     * add values without copying them
     *
     * @param row a row to insert, values are not copied!
     */
    public void add(Value[] row) {
        values.add(row);
    }

    /**
     * provides the values
     *
     * @param rowIndex    the wow
     * @param columnIndex the column
     * @return the value stored at the given position
     */
    public Value getValue(int rowIndex, int columnIndex) {
        return values.get(rowIndex)[columnIndex];
    }

    /**
     * the number of signals
     * @return the column count
     */
    public int getColumns() {
        return names.size();
    }

    /**
     * Returns the column names
     * @param col the column
     * @return the name
     */
    public String getColumnName(int col) {
        return names.get(col);
    }
}
