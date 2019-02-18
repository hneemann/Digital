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
    private Value[] values;
    private int lineNum;

    /**
     * Creates a new instance
     *
     * @param values  the values
     * @param lineNum the line number in the source file
     */
    public TestRow(Value[] values, int lineNum) {
        this.values = values;
        this.lineNum = lineNum;
    }

    /**
     * Creates a new instance
     *
     * @param values the values
     */
    public TestRow(Value... values) {
        this.values = values;
        this.lineNum = -1;
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
     * @return the line number
     */
    public int getLineNum() {
        return lineNum;
    }
}
