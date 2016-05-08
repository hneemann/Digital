package de.neemann.digital.analyse.quinemc;

/**
 * @author hneemann
 */
public enum ThreeStateValue {
    /**
     * zero or false
     */
    zero,
    /**
     * one or true
     */
    one,
    /**
     * dont care
     */
    dontCare;


    /**
     * Create a value from a bool
     *
     * @param bool the bool
     * @return the created ThreeStateValue
     */
    public static ThreeStateValue value(boolean bool) {
        if (bool) {
            return one;
        } else {
            return zero;
        }
    }

    /**
     * Create a value from an int
     * 0 and 1 work as expected, any other value means "dont care"
     *
     * @param value the value
     * @return the created ThreeStateValue
     */
    public static ThreeStateValue value(int value) {
        switch (value) {
            case 0:
                return ThreeStateValue.zero;
            case 1:
                return ThreeStateValue.one;
            default:
                return ThreeStateValue.dontCare;
        }
    }

    /**
     * this value as an integer
     *
     * @return the int value, 2 mans "don't care"
     */
    public int asInt() {
        return ordinal();
    }
}
