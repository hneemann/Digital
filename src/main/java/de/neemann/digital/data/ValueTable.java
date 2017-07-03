package de.neemann.digital.data;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Stores values in a table
 * Created by hneemann on 03.07.17.
 */
public class ValueTable implements Iterable<Value[]> {

    private final String[] names;
    private final ArrayList<Value[]> values;
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
     * @return number of rows
     */
    public int getRows() {
        return values.size();
    }

    /**
     * add values without copying them
     *
     * @param row a row to insert, values are not copied!
     * @return this for chained calls
     */
    public ValueTable add(Value[] row) {
        if (maxSize > 0 && values.size() >= maxSize) {
            while (values.size() >= maxSize)
                values.remove(0);
            Arrays.fill(max, 0);
            for (Value[] v : values)
                checkMax(v);
        }
        values.add(row);
        checkMax(row);
        return this;
    }

    private void checkMax(Value[] row) {
        for (int i = 0; i < row.length; i++)
            if (max[i] < row[i].getValue()) max[i] = row[i].getValue();
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
    public Iterator<Value[]> iterator() {
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
        saveCSV(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8")));
    }

    /**
     * Stores the data in  csv file
     *
     * @param w the writer
     * @throws IOException IOException
     */
    public void saveCSV(BufferedWriter w) throws IOException {
        try {
            w.write("\"step\"");
            for (String s : names)
                w.write(",\"" + s + '"');
            w.write("\n");
            int row = 0;
            for (Value[] s : this) {
                w.write("\"" + (row++) + "\"");
                for (Value value : s) w.write(",\"" + value + "\"");
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
}
