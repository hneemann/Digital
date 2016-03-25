package de.neemann.digital.core.memory;

/**
 * @author hneemann
 */
public class DataField {

    public static final DataField DEFAULT = new DataField();

    private long[] data;

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

}
