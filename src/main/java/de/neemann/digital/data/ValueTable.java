/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.data;

import de.neemann.digital.StringList;
import de.neemann.digital.core.IntFormat;
import de.neemann.digital.core.Observable;
import de.neemann.digital.core.ValueFormatter;
import de.neemann.digital.testing.parser.TestRow;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Stores values in a table
 */
public class ValueTable extends Observable implements Iterable<TestRow> {

    private final String[] names;
    private final ArrayList<TestRow> values;
    private ArrayList<Integer> tableRowIndex;
    private final long[] max;
    private int maxSize = 0;

    /**
     * Creates a new table.
     *
     * @param names the signal names
     */
    public ValueTable(ArrayList<String> names) {
        this(names.toArray(new String[names.size()]));
    }

    /**
     * Creates a new table.
     *
     * @param names the signal names
     */
    public ValueTable(String... names) {
        this.names = names;
        values = new ArrayList<>();
        max = new long[names.length];
    }

    /**
     * Creates a copy of the given ValueTable
     *
     * @param toCopy the ValueTable to copy
     */
    public ValueTable(ValueTable toCopy) {
        this.names = toCopy.names;
        values = (ArrayList<TestRow>) toCopy.values.clone();
        max = toCopy.max.clone();
    }

    /**
     * @return number of rows
     */
    public int getRows() {
        return values.size();
    }

    /**
     * @return number of rows in a table
     */
    public int getTableRows() {
        if (tableRowIndex == null)
            return values.size();
        else
            return tableRowIndex.size();
    }

    /**
     * add values without copying them
     *
     * @param row a row to insert, values are not copied!
     * @return this for chained calls
     */
    public ValueTable add(TestRow row) {
        if (maxSize > 0 && values.size() >= maxSize) {

            if (tableRowIndex != null)
                throw new RuntimeException("delete not allowed if table index is present");

            while (values.size() >= maxSize)
                values.remove(0);
        }
        if (tableRowIndex != null)
            tableRowIndex.add(values.size());
        values.add(row);

        checkMax(row.getValues());

        fireHasChanged();

        return this;
    }

    /**
     * omit the last added value in a table representation
     *
     * @return this for chained calls
     */
    public ValueTable omitInTable() {
        if (tableRowIndex == null) {
            tableRowIndex = new ArrayList<>();
            for (int i = 0; i < values.size(); i++)
                tableRowIndex.add(i);
        }
        tableRowIndex.remove(tableRowIndex.size() - 1);
        return this;
    }


    private void checkMax(Value[] row) {
        for (int i = 0; i < row.length; i++)
            if (Long.compareUnsigned(max[i], row[i].getValue()) < 0) max[i] = row[i].getValue();
    }

    /**
     * provides the values
     *
     * @param rowIndex    the row
     * @param columnIndex the column
     * @return the value stored at the given position
     */
    public Value getValue(int rowIndex, int columnIndex) {
        return values.get(rowIndex).getValue(columnIndex);
    }

    /**
     * provides the values for the use in a table
     *
     * @param rowIndex    the row
     * @param columnIndex the column
     * @return the value stored at the given position
     */
    public Value getTableValue(int rowIndex, int columnIndex) {
        return getTableRow(rowIndex).getValue(columnIndex);
    }

    /**
     * Returns a table row
     *
     * @param rowIndex the index of the table row
     * @return the table row
     */
    public TestRow getTableRow(int rowIndex) {
        if (tableRowIndex == null)
            return values.get(rowIndex);
        else
            return values.get(tableRowIndex.get(rowIndex));
    }

    /**
     * Returns the rows description
     *
     * @param rowIndex the row index
     * @return the source line number
     */
    public String getDescription(int rowIndex) {
        if (tableRowIndex == null)
            return values.get(rowIndex).getDescription();
        else
            return values.get(tableRowIndex.get(rowIndex)).getDescription();
    }

    /**
     * the number of signals
     *
     * @return the column count
     */
    public int getColumns() {
        return names.length;
    }

    /**
     * Returns the column names
     *
     * @param col the column
     * @return the name
     */
    public String getColumnName(int col) {
        return names[col];
    }

    @Override
    public Iterator<TestRow> iterator() {
        return values.iterator();
    }

    /**
     * Returns the max value stored in the given column
     *
     * @param col the column
     * @return the max value
     */
    public long getMax(int col) {
        return max[col];
    }

    /**
     * Stores the data in  csv file
     *
     * @param file the file
     * @throws IOException IOException
     */
    public void saveCSV(File file) throws IOException {
        saveCSV(file, null);
    }

    /**
     * Stores the data in  csv file
     *
     * @param file       the file
     * @param columnInfo information of how to format the values, maybe null
     * @throws IOException IOException
     */
    public void saveCSV(File file, ColumnInfo[] columnInfo) throws IOException {
        saveCSV(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)), columnInfo);
    }

    /**
     * Stores the data in  csv file
     *
     * @param w the writer
     * @throws IOException IOException
     */
    public void saveCSV(BufferedWriter w) throws IOException {
        saveCSV(w, null);
    }

    void saveCSV(BufferedWriter w, ColumnInfo[] columnInfo) throws IOException {
        try {
            w.write("\"step\"");
            for (String s : names)
                w.write(",\"" + s + '"');
            w.write("\n");
            int row = 0;
            for (TestRow s : this) {
                w.write("\"" + (row++) + "\"");
                if (columnInfo == null) {
                    for (Value value : s.getValues())
                        w.write(",\"" + value + "\"");
                } else {
                    int i = 0;
                    for (Value value : s.getValues())
                        w.write(",\"" + columnInfo[i++].format(value) + "\"");
                }
                w.write("\n");
            }
        } finally {
            w.close();
        }
    }

    /**
     * clear all values
     */
    public void clear() {
        values.clear();
        Arrays.fill(max, 0);
        fireHasChanged();
    }

    /**
     * set the maximum size for this table
     *
     * @param maxSize the max size
     * @return this for chained calls
     */
    public ValueTable setMaxSize(int maxSize) {
        this.maxSize = maxSize;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        StringList sl = new StringList(sb);
        for (String n : names)
            sl.add(n);
        sb.append("\n");

        if (tableRowIndex == null)
            for (TestRow row : values) {
                sl = new StringList(sb);
                for (Value v : row.getValues())
                    sl.add(v.toString());
                sb.append("\n");
            }
        else
            for (int i : tableRowIndex) {
                sl = new StringList(sb);
                for (Value v : values.get(i).getValues())
                    sl.add(v.toString());
                sb.append("\n");
            }

        return sb.toString();
    }

    /**
     * Columns formatting information
     */
    public static final class ColumnInfo {
        private final int bits;
        private final ValueFormatter format;

        /**
         * Creates a new instance
         *
         * @param format the format to use
         * @param bits   the number of bits to output
         */
        public ColumnInfo(ValueFormatter format, int bits) {
            if (format == null)
                format = IntFormat.HEX_FORMATTER;
            else if (format.equals(IntFormat.DEFAULT_FORMATTER) && (bits > 3))
                format = IntFormat.HEX_FORMATTER;
            this.format = format;
            this.bits = bits;
        }

        private String format(Value value) {
            switch (value.getType()) {
                case HIGHZ:
                    return "Z";
                case DONTCARE:
                    return "X";
                case CLOCK:
                    return "C";
                default:
                    return format.formatToEdit(new de.neemann.digital.core.Value(value.getValue(), bits));
            }
        }
    }
}
