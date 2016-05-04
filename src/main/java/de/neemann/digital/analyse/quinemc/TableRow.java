package de.neemann.digital.analyse.quinemc;


import de.neemann.digital.analyse.expression.BitSetter;
import de.neemann.digital.analyse.expression.Constant;
import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.Variable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.TreeSet;

import static de.neemann.digital.analyse.expression.Not.not;
import static de.neemann.digital.analyse.expression.Operation.and;

/**
 * Represents a row in a QMC table
 *
 * @author hneemann
 */
public class TableRow implements Comparable<TableRow> {

    private final TableItem[] items;
    private boolean used = false;
    private final TreeSet<Integer> source;

    /**
     * Copies the given table row
     *
     * @param tr the row to copy
     */
    public TableRow(TableRow tr) {
        this(tr.size());
        for (int i = 0; i < size(); i++)
            items[i] = tr.get(i);
    }

    /**
     * Creates a new tyble row
     *
     * @param cols number of columns
     */
    public TableRow(int cols) {
        items = new TableItem[cols];
        source = new TreeSet<>();
    }

    /**
     * Creates a new row
     *
     * @param cols     the number of columns
     * @param bitValue the value representing the bits in the row
     * @param index    the index of the original source row
     * @param dontCare dont care
     */
    public TableRow(int cols, int bitValue, int index, boolean dontCare) {
        this(cols);
        if (!dontCare)
            source.add(index);
        new BitSetter(cols) {
            @Override
            public void setBit(int row, int bit, boolean value) {
                if (value)
                    items[bit] = TableItem.one;
                else
                    items[bit] = TableItem.zero;
            }
        }.fill(bitValue);
    }

    /**
     * The item at the given indes
     *
     * @param index the comumns index
     * @return the value
     */
    public TableItem get(int index) {
        return items[index];
    }

    /**
     * Sets the given idex to optimized
     *
     * @param index the columns index
     */
    public void setToOptimized(int index) {
        items[index] = TableItem.optimized;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int c = 0; c < items.length; c++)
            switch (items[c]) {
                case zero:
                    sb.append('0');
                    break;
                case one:
                    sb.append('1');
                    break;
                case optimized:
                    sb.append('-');
                    break;
            }
        for (Integer i : source)
            sb.append(",").append(i);
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TableRow tableRow = (TableRow) o;

        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(items, tableRow.items);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(items);
    }

    /**
     * Set the used flag
     */
    public void setUsed() {
        this.used = true;
    }

    /**
     * @return the used flag
     */
    public boolean isUsed() {
        return used;
    }

    /**
     * @return the number of one values in this row
     */
    public int countOnes() {
        int c = 0;
        for (int i = 0; i < items.length; i++)
            if (items[i] == TableItem.one)
                c++;
        return c;
    }

    @Override
    public int compareTo(TableRow tableRow) {
        return Integer.compare(countOnes(), tableRow.countOnes());
    }

    /**
     * @return the number of columns
     */
    public int size() {
        return items.length;
    }

    /**
     * @return the source line numbers
     */
    public Collection<Integer> getSource() {
        return source;
    }

    /**
     * Adds some sources to this line
     *
     * @param s the sources to add
     */
    public void addSource(Collection<Integer> s) {
        source.addAll(s);
    }

    /**
     * Returns an expression build with the given variables
     *
     * @param vars the variables to use
     * @return the expression
     */
    public Expression getExpression(ArrayList<Variable> vars) {
        Expression e = null;
        for (int i = 0; i < size(); i++) {
            Expression term = null;
            switch (items[i]) {
                case one:
                    term = vars.get(i);
                    break;
                case zero:
                    term = not(vars.get(i));
                    break;
            }
            if (term != null) {
                if (e == null)
                    e = term;
                else
                    e = and(e, term);
            }
        }
        if (e == null)
            return Constant.ONE;
        else
            return e;
    }

}
