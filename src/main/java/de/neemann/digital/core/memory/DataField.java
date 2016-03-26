package de.neemann.digital.core.memory;

/**
 * @author hneemann
 */
public class DataField {

    public static final DataField DEFAULT = new DataField(0);

    private long[] data;

    public DataField(int size) {
        this.data = new long[size];
    }

    public DataField(DataField dataField) {
        this(dataField, dataField.size());
    }

    public DataField(DataField dataField, int size) {
        data = new long[size];
        System.arraycopy(dataField.data, 0, data, 0, Math.min(size, dataField.size()));
    }

    public void setData(int addr, long value) {
        if (addr < data.length)
            data[addr] = value;
    }

    public long getData(int addr) {
        if (addr >= data.length)
            return 0;
        else
            return data[addr];
    }

    public int size() {
        return data.length;
    }
}
