package de.neemann.digital.core;

/**
 * represents a simple value
 *
 * @author hneemann
 */
public class Value {
    protected final int bits;
    protected boolean highZ;
    protected long value;

    /**
     * Creates a new value
     *
     * @param bits the number of bits
     */
    public Value(int bits) {
        this(bits, false);
    }

    /**
     * Creates a new value
     *
     * @param bits  the number of bits
     * @param highZ if true the value is a high z value
     */
    public Value(int bits, boolean highZ) {
        this.bits = bits;
        this.highZ = highZ;
    }

    /**
     * Sets the value
     *
     * @param value value
     * @param highZ highZ
     */
    public void set(long value, boolean highZ) {
        this.value = value;
        this.highZ = highZ;
    }

    /**
     * Sets the value to another value
     *
     * @param v the other value
     */
    public void set(Value v) {
        set(v.value, v.highZ);
    }
}
