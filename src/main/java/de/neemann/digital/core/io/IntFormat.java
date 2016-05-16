package de.neemann.digital.core.io;


import de.neemann.digital.core.ObservableValue;

/**
 * @author hneemann
 */
public enum IntFormat {
    /**
     * the default format as defined in {@link ObservableValue#getValueString()}
     */
    def,
    /**
     * decimal
     */
    dec,
    /**
     * hexadecimal
     */
    hex,
    /**
     * binary
     */
    bin,
    /**
     * ascii format
     */
    ascii;

    /**
     * Formats the value
     *
     * @param inValue the value to format
     * @return the formatted value as a string
     */
    public String format(ObservableValue inValue) {
        if (inValue.isHighZ())
            return "HZ";

        switch (this) {
            case dec:
                return Long.toString(inValue.getValue());
            case hex:
                return Long.toHexString(inValue.getValue()).toUpperCase();
            case bin:
                return Long.toBinaryString(inValue.getValue());
            case ascii:
                return "" + (char) inValue.getValue();
            default:
                return inValue.getValueString();
        }
    }
}
