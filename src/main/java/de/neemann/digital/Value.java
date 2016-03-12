package de.neemann.digital;

/**
 * @author hneemann
 */
public class Value {
    protected final int bits;
    protected boolean highZ;
    protected int value;

    public Value(int bits) {
        this(bits, false);
    }

    public Value(int bits, boolean highZ) {
        this.bits = bits;
        this.highZ = highZ;
    }

    public void set(int value, boolean highZ) {
        this.value = value;
        this.highZ = highZ;
    }

    public void set(Value v) {
        set(v.value, v.highZ);
    }
}
