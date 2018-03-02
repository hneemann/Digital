/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.quinemc;


import de.neemann.digital.analyse.expression.Constant;
import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.Variable;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import static de.neemann.digital.analyse.expression.Not.not;
import static de.neemann.digital.analyse.expression.Operation.and;

/**
 * Represents a row in a QMC table
 */
public final class TableRow implements Comparable<TableRow> {

    private final TreeSet<Integer> source;
    private boolean used = false;
    private long optimizedFlags;
    private long state;
    private int cols;

    /**
     * Copies the given table row
     *
     * @param tr the row to copy
     */
    public TableRow(TableRow tr) {
        this(tr.size());
        state = tr.state;
        optimizedFlags = tr.optimizedFlags;
    }

    /**
     * Creates a new table row
     *
     * @param cols number of columns
     */
    public TableRow(int cols) {
        this.cols = cols;
        source = new TreeSet<>();
    }

    /**
     * Creates a new row
     *
     * @param cols     the number of columns
     * @param bitValue the value representing the bits in the row
     * @param index    the index of the original source row
     * @param dontCare true if don't care
     */
    public TableRow(int cols, int bitValue, int index, boolean dontCare) {
        this(cols, bitValue);
        if (!dontCare)
            source.add(index);
    }

    /**
     * Creates a new row.
     * Used only for exact cover tests!
     *
     * @param cols     the number of columns
     * @param bitValue the value representing the bits in the row
     */
    public TableRow(int cols, int bitValue) {
        this(cols);
        state = Integer.reverse(bitValue) >>> (32 - cols);
    }


    /**
     * Sets the given index to optimized
     *
     * @param index the columns index
     */
    public void setToOptimized(int index) {
        state &= ~(1L << index);
        optimizedFlags |= 1L << index;
    }

    /**
     * Returns the optimized flags.
     * All Variables which are deleted/optimized in this row are marked by a one bit at their position.
     *
     * @return the flags
     */
    public long getOptimizedFlags() {
        return optimizedFlags;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int c = 0; c < cols; c++) {
            long mask = 1L << c;

            if ((optimizedFlags & mask) != 0)
                sb.append('-');
            else if ((state & mask) != 0)
                sb.append('1');
            else
                sb.append('0');
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

        return optimizedFlags == tableRow.optimizedFlags && state == tableRow.state;
    }

    @Override
    public int hashCode() {
        int result = (int) (optimizedFlags ^ (optimizedFlags >>> 32));
        result = 31 * result + (int) (state ^ (state >>> 32));
        result = 31 * result + cols;
        return result;
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

    @Override
    public int compareTo(TableRow tableRow) {
        int e = Long.compare(optimizedFlags, tableRow.optimizedFlags);
        if (e == 0)
            return Long.compare(state, tableRow.state);
        return e;
    }

    /**
     * @return the number of columns
     */
    public int size() {
        return cols;
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
     * Adds some sources to this line
     *
     * @param s the sources to add
     * @return this for chained calls
     */
    public TableRow addSource(Integer... s) {
        addSource(Arrays.asList(s));
        return this;
    }

    /**
     * Returns an expression build with the given variables
     *
     * @param vars the variables to use
     * @return the expression
     */
    public Expression getExpression(List<Variable> vars) {
        Expression e = null;
        for (int i = 0; i < size(); i++) {
            long mask = 1L << i;
            if ((optimizedFlags & mask) == 0) {
                Expression term;
                if ((state & mask) == 0)
                    term = not(vars.get(i));
                else
                    term = vars.get(i);

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

    /**
     * Check if rows differ in only one therm
     *
     * @param r2 the other row
     * @return the matching literal or -1
     */
    public int checkCompatible(TableRow r2) {
        if (optimizedFlags != r2.optimizedFlags)
            return -1;

        long v = state ^ r2.state;
        if (Long.bitCount(v) != 1)
            return -1;

        return Long.numberOfTrailingZeros(v);
    }
}
