package de.neemann.digital.core;

/**
 * Helper for bit manipulating
 */
public final class Bits {

    private Bits() {
    }

    /**
     * Shifts a value up
     *
     * @param val  the value to shift
     * @param bits the bit count to shift
     * @return the shifted value
     */
    public static long up(long val, int bits) {
        if (bits < 64)
            return val << bits;
        else
            return 0;
    }

    /**
     * Shifts a value down
     *
     * @param val  the value to shift
     * @param bits the bit count to shift
     * @return the shifted value
     */
    public static long down(long val, int bits) {
        if (bits < 64)
            return val >>> bits;
        else
            return 0;
    }

    /**
     * Creates a bit mask with the lowest [bits] bits set.
     *
     * @param bits the number of 1 bits
     * @return a value with the lowest [bits] bits set.
     */
    public static long mask(int bits) {
        if (bits < 64)
            return (1L << bits) - 1;
        else
            return -1;
    }

    /**
     * calculates the signed flag.
     *
     * @param bits the bit count
     * @return the last used bit ( 1<<(bits-1) )
     */
    public static long signedFlagMask(int bits) {
        return up(1, bits - 1);
    }
}
