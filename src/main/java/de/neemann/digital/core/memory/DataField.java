package de.neemann.digital.core.memory;

import de.neemann.digital.lang.Lang;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author hneemann
 */
public class DataField {

    public static final DataField DEFAULT = new DataField(0, 8);

    private final int size;
    private long[] data;
    private final int bits;

    private transient ArrayList<DataListener> listeners;

    public DataField(int size, int bits) {
        this(new long[size], size, bits);
    }

    private DataField(long[] data, int size, int bits) {
        this.size = size;
        this.data = data;
        this.bits = bits;
    }

    public DataField(DataField dataField, int newSize, int bits) {
        this(Arrays.copyOf(dataField.data, newSize), newSize, bits);
    }

    public DataField(File file) throws IOException {
        this(new InputStreamReader(new FileInputStream(file), "UTF-8"));
    }

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

    public long getData(int addr) {
        if (addr >= data.length)
            return 0;
        else
            return data[addr];
    }

    public int size() {
        return size;
    }

    public DataField getMinimized() {
        int pos = data.length;
        while (pos > 0 && data[pos - 1] == 0) pos--;
        if (pos == data.length)
            return this;
        else
            return new DataField(Arrays.copyOf(data, pos), size, bits);
    }

    public int getBits() {
        return bits;
    }

    public void addListener(DataListener l) {
        if (listeners == null)
            listeners = new ArrayList<>();
        listeners.add(l);
    }

    public void removeListener(DataListener l) {
        if (listeners == null)
            return;

        listeners.remove(l);
        if (listeners.isEmpty())
            listeners = null;
    }

    public void fireChanged(int addr) {
        if (listeners == null)
            return;

        for (DataListener l : listeners)
            l.valueChanged(addr);
    }


    public interface DataListener {
        void valueChanged(int addr);
    }
}
