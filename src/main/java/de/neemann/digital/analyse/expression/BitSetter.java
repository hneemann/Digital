package de.neemann.digital.analyse.expression;

/**
 * Used to fill a table.
 * The first section of input values is set by the {@link BitSetter}
 *
 * @author hneemann
 */
public abstract class BitSetter {

    private final int bitCount;

    /**
     * Creates a new instance
     *
     * @param bitCount th number of bits
     */
    public BitSetter(int bitCount) {
        this.bitCount = bitCount;
    }

    /**
     * Fills the row value
     *
     * @param row the row
     */
    public void fill(int row) {
        int mask = 1 << (bitCount - 1);
        for (int bit = 0; bit < bitCount; bit++) {
            setBit(row, bit, (row & mask) > 0);
            mask >>= 1;
        }
    }

    /**
     * Gets a single bit
     *
     * @param row the row
     * @param bit the var number
     * @return the bit value
     */
    public boolean getBit(int row, int bit) {
        int mask = 1 << (bitCount - 1);
        mask >>= bit;
        return (row & mask) > 0;
    }

    /**
     * Used to fill the row
     *
     * @param row   the row number
     * @param bit   the bit to set, refers to the variable number
     * @param value the value
     */
    public abstract void setBit(int row, int bit, boolean value);
}
