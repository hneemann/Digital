/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.memory;

import de.neemann.digital.core.Bits;
import de.neemann.digital.hdl.hgs.HGSArray;
import de.neemann.digital.lang.Lang;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 */
public class DataField implements HGSArray {

    /***
     * Simple default data field
     */
    public static final DataField DEFAULT = new DataField(0);

    private final int size;
    private long[] data;

    private final transient ArrayList<DataListener> listeners = new ArrayList<>();

    /**
     * Creates a new DataField
     *
     * @param size size
     */
    public DataField(int size) {
        this(new long[size], size);
    }

    /**
     * Creates a new data field
     *
     * @param data the data
     * @param size the size
     */
    public DataField(long[] data, int size) {
        this.size = size;
        this.data = data;
    }

    /**
     * Create a new instance based on a given instance.
     * The data given is copied.
     *
     * @param dataField the data to use
     * @param newSize   new size
     */
    public DataField(DataField dataField, int newSize) {
        this(Arrays.copyOf(dataField.data, newSize), newSize);
    }

    /**
     * Creates a new instance and fills it with the data in the given file
     *
     * @param file the file containing the data
     * @throws IOException IOException
     */
    public DataField(File file) throws IOException {
        this(new InputStreamReader(new FileInputStream(file), "UTF-8"));
    }

    /**
     * Creates a new instance and fills it with the data in the given reader
     *
     * @param reader the reader
     * @throws IOException IOException
     */
    public DataField(Reader reader) throws IOException {
        try (BufferedReader br = new BufferedReader(reader)) {
            data = new long[1024];
            String header = br.readLine();
            if (header == null || !header.equals("v2.0 raw"))
                throw new IOException(Lang.get("err_invalidFileFormat"));
            String line;
            int pos = 0;
            while ((line = br.readLine()) != null) {
                try {
                    int p = line.indexOf('#');
                    if (p >= 0)
                        line = line.substring(0, p).trim();
                    else
                        line = line.trim();
                    if (line.length() > 0) {
                        long v = Bits.decode(line, 0, 16);
                        if (pos == data.length)
                            data = Arrays.copyOf(data, data.length * 2);
                        data[pos] = v;
                        pos++;
                    }
                } catch (Bits.NumberFormatException e) {
                    throw new IOException(e);
                }
            }
            size = pos;
        }
    }

    /**
     * Save the stored data
     *
     * @param file file to store the data to
     * @throws IOException IOException
     */
    public void saveTo(File file) throws IOException {
        DataField df = getMinimized();
        try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "utf-8"))) {
            w.write("v2.0 raw");
            w.newLine();
            for (long l : df.getData()) {
                w.write(Long.toHexString(l));
                w.newLine();
            }
        }
    }

    /**
     * Sets all stored data to null!
     * Is not called during simulation! Is only called during editing.
     */
    public void clearAll() {
        if (data != null)
            Arrays.fill(data, 0);

        // all the data have changed!
        fireChanged(-1);
    }

    /**
     * Sets a data value the DataField
     *
     * @param addr  the address
     * @param value the value
     * @return this for chained calls
     */
    public DataField setData(int addr, long value) {
        if (addr < size) {
            if (addr >= data.length)
                data = Arrays.copyOf(data, size);

            if (data[addr] != value) {
                data[addr] = value;
                fireChanged(addr);
            }
        }
        return this;
    }

    /**
     * Gets the value at the given address
     *
     * @param addr the address
     * @return the value
     */
    public long getDataWord(int addr) {
        if (addr >= data.length)
            return 0;
        else
            return data[addr];
    }

    /**
     * Returns the size of this field
     *
     * @return the size
     */
    public int size() {
        return size;
    }

    /**
     * Returns a new minimal {@link DataField}.
     * All trailing zeros are removed.
     *
     * @return the new {@link DataField}
     */
    public DataField getMinimized() {
        int pos = data.length;
        while (pos > 0 && data[pos - 1] == 0) pos--;
        if (pos == data.length)
            return this;
        else
            return new DataField(Arrays.copyOf(data, pos), size);
    }

    /**
     * Adds a listener to this DataField
     *
     * @param l the listener
     */
    public void addListener(DataListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    /**
     * Removes a listener
     *
     * @param l the listener to remove
     */
    public void removeListener(DataListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    /**
     * Fires a valueChanged event
     *
     * @param addr the address which value has changed
     */
    private void fireChanged(int addr) {
        synchronized (listeners) {
            for (DataListener l : listeners)
                l.valueChanged(addr);
        }
    }

    /**
     * Sets the data from the given data field
     *
     * @param dataField the data to set to this data field
     */
    public void setDataFrom(DataField dataField) {
        data = Arrays.copyOf(dataField.data, size);
        fireChanged(-1);
    }

    @Override
    public int hgsArraySize() {
        return data.length;
    }

    @Override
    public Object hgsArrayGet(int i) {
        return getDataWord(i);
    }

    /**
     * The listener interface
     */
    public interface DataListener {
        /**
         * Called if the DataField has changed.
         *
         * @param addr the address which has changed, Called with addr=-1 if all values have changed!
         */
        void valueChanged(int addr);
    }

    /**
     * @return the raw data
     */
    public long[] getData() {
        return data;
    }
}
