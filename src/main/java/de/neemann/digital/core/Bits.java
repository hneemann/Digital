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

    /**
     * Returns true if value is negative
     *
     * @param value the value
     * @param bits  the bit count
     * @return true if the last relevant bit is set
     */
    public static boolean isNegative(long value, int bits) {
        return (value & signedFlagMask(bits)) != 0;
    }


    /**
     * Calculates the number of bits needed to store the given value b.
     *
     * @param b number
     * @return number of bits needed to store b
     */
    public static int binLn2(int b) {
        int outBits = 1;
        while ((1 << outBits) <= b)
            outBits++;
        return outBits;
    }
}
