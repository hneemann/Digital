package de.neemann.digital.core.memory;

import de.neemann.digital.lang.Lang;

import java.io.*;
import java.util.Arrays;

/**
 * @author hneemann
 */
public class DataField {

    public static final DataField DEFAULT = new DataField(0);

    private final int size;
    private long[] data;

    public DataField(int size) {
        this(new long[size], size);
    }

    private DataField(long[] data, int size) {
        this.size = size;
        this.data = data;
    }

    public DataField(DataField dataField, int newSize) {
        this(Arrays.copyOf(dataField.data, newSize), newSize);
    }

    public DataField(File file) throws IOException {
        this(new FileReader(file));
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
    }

    public void setData(int addr, long value) {
        if (addr < size) {
            if (addr >= data.length)
                data = Arrays.copyOf(data, size);
            data[addr] = value;
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
            return new DataField(Arrays.copyOf(data, pos), size);
    }
}
