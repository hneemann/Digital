package de.neemann.digital.core;

/**
 * @author hneemann
 */
public class Value {
    protected final int bits;
    protected boolean highZ;
    protected long value;

    public Value(int bits) {
        this(bits, false);
    }

    public Value(int bits, boolean highZ) {
        this.bits = bits;
        this.highZ = highZ;
    }

    public void set(long value, boolean highZ) {
        this.value = value;
        this.highZ = highZ;
    }

    public void set(Value v) {
        set(v.value, v.highZ);
    }
}
