/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.memory;

import de.neemann.digital.hdl.hgs.HGSArray;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Used to store an array of values.
 */
public class DataField implements HGSArray {

    /***
     * Simple default data field
     */
    public static final DataField DEFAULT = new DataField(0);

    private long[] data;

    private final transient ArrayList<DataListener> listeners = new ArrayList<>();

    /**
     * Creates a new DataField
     *
     * @param size size
     */
    public DataField(int size) {
        this(new long[size]);
    }

    /**
     * Creates a new data field
     *
     * @param data the data to copy
     */
    public DataField(DataField data) {
        this.data = Arrays.copyOf(data.data, data.data.length);
    }

    /**
     * Creates a new data field
     *
     * @param data the data
     */
    public DataField(long[] data) {
        this.data = data;
    }

    /**
     * Save the stored data
     *
     * @param file file to store the data to
     * @throws IOException IOException
     */
    public void saveTo(File file) throws IOException {
        try (Writer w = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            saveTo(w);
        }
    }

    /**
     * Save the stored data
     *
     * @param writer writer to write the data to
     * @throws IOException IOException
     */
    public void saveTo(Writer writer) throws IOException {
        trim();
        BufferedWriter w;
        if (writer instanceof BufferedWriter)
            w = (BufferedWriter) writer;
        else
            w = new BufferedWriter(writer);

        w.write("v2.0 raw");
        w.newLine();
        if (data.length > 0) {
            long akt = data[0];
            int count = 1;
            for (int i = 1; i < data.length; i++) {
                final long now = data[i];
                if (now == akt)
                    count++;
                else {
                    writeChunk(w, akt, count);
                    akt = now;
                    count = 1;
                }
            }
            writeChunk(w, akt, count);
        }
        w.flush();
    }

    private void writeChunk(BufferedWriter w, long data, int count) throws IOException {
        if (count < 4) {
            for (int j = 0; j < count; j++) {
                w.write(Long.toHexString(data));
                w.newLine();
            }
        } else {
            w.write(Integer.toString(count));
            w.write('*');
            w.write(Long.toHexString(data));
            w.newLine();
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
     * Sets a data value the DataField.
     * If the actual data field capacity is to small the size in increased.
     *
     * @param addr  the address
     * @param value the value
     * @return this for chained calls
     */
    public DataField setData(int addr, long value) {
        if (addr >= data.length) {
            int newLen = addr * 2;
            if (newLen < 32) newLen = 32;
            data = Arrays.copyOf(data, newLen);
        }

        if (data[addr] != value) {
            data[addr] = value;
            fireChanged(addr);
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
     * Trims the data field to it's minimal size
     * All trailing zeros are removed.
     *
     * @return the new length of the data array
     */
    public int trim() {
        int pos = data.length;
        while (pos > 0 && data[pos - 1] == 0) pos--;
        if (pos < data.length)
            data = Arrays.copyOf(data, pos);
        return data.length;
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
        data = Arrays.copyOf(dataField.data, dataField.data.length);
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
