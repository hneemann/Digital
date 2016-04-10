package de.neemann.digital.core.memory;

import de.neemann.digital.lang.Lang;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author hneemann
 */
public class DataField {

    /***
     * Simple default data field
     */
    public static final DataField DEFAULT = new DataField(0, 8);

    private final int size;
    private long[] data;
    private final int bits;

    private transient ArrayList<DataListener> listeners;

    /**
     * Creates a new DataField
     *
     * @param size size
     * @param bits number of bits
     */
    public DataField(int size, int bits) {
        this(new long[size], size, bits);
    }

    private DataField(long[] data, int size, int bits) {
        this.size = size;
        this.data = data;
        this.bits = bits;
    }

    /**
     * Create a new instance based on a given instance.
     * The data given is copied.
     *
     * @param dataField the data to use
     * @param newSize   new size
     * @param bits      number og bits
     */
    public DataField(DataField dataField, int newSize, int bits) {
        this(Arrays.copyOf(dataField.data, newSize), newSize, bits);
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
                    long v = Long.parseLong(line, 16);
                    if (pos == data.length)
                        data = Arrays.copyOf(data, data.length * 2);
                    data[pos] = v;
                    pos++;
                } catch (NumberFormatException e) {
                    throw new IOException(e);
                }
            }
            size = pos;
        }
        bits = 16;
    }

    /**
     * Sets a data value the DataField
     *
     * @param addr  the address
     * @param value the value
     */
    public void setData(int addr, long value) {
        if (addr < size) {
            if (addr >= data.length)
                data = Arrays.copyOf(data, size);

            if (data[addr] != value) {
                data[addr] = value;
                fireChanged(addr);
            }
        }
    }

    /**
     * Gets the value at the given address
     *
     * @param addr the address
     * @return the value
     */
    public long getData(int addr) {
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
            return new DataField(Arrays.copyOf(data, pos), size, bits);
    }

    /**
     * @return the number of bits
     */
    public int getBits() {
        return bits;
    }

    /**
     * Adds a listener to this DataField
     *
     * @param l the listener
     */
    public void addListener(DataListener l) {
        if (listeners == null)
            listeners = new ArrayList<>();
        listeners.add(l);
    }

    /**
     * Removes a listener
     *
     * @param l the listener to remove
     */
    public void removeListener(DataListener l) {
        if (listeners == null)
            return;

        listeners.remove(l);
        if (listeners.isEmpty())
            listeners = null;
    }

    /**
     * Fires a valueChanged event
     *
     * @param addr the address which value has changed
     */
    public void fireChanged(int addr) {
        if (listeners == null)
            return;

        for (DataListener l : listeners)
            l.valueChanged(addr);
    }

    /**
     * The listener interface
     */
    public interface DataListener {
        /**
         * Called if the DataField has changed
         *
         * @param addr the address which has changed
         */
        void valueChanged(int addr);
    }
}
