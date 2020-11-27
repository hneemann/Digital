/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.testing.parser;

import de.neemann.digital.data.Value;

/**
 * A test data row, used by the {@link LineListener}
 */
public class TestRow {
    private final Value[] values;
    private final String description;
    private int rowCount = -1;

    /**
     * Creates a new instance
     *
     * @param values      the values
     * @param description the context of the row
     */
    public TestRow(Value[] values, String description) {
        this.values = values;
        this.description = description;
    }

    /**
     * Creates a new instance
     *
     * @param values the values
     */
    public TestRow(Value... values) {
        this.values = values;
        this.description = null;
    }

    /**
     * @return the array of values
     */
    public Value[] getValues() {
        return values;
    }

    /**
     * Returns the value with index i
     *
     * @param i the index
     * @return the value
     */
    public Value getValue(int i) {
        return values[i];
    }

    /**
     * @return the rows description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the row number
     *
     * @param rowCount the number to set
     * @return this for chained calls
     */
    public TestRow setRow(int rowCount) {
        this.rowCount = rowCount;
        return this;
    }

    /**
     * @return the row number of this row, or -1 if no row number is available
     */
    public int getRow() {
        return rowCount;
    }
}
