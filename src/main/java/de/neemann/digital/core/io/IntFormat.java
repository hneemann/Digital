package de.neemann.digital.core.io;


import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.Value;

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
    public String format(Value inValue) {
        if (inValue.isHighZ())
            return "HZ";

        switch (this) {
            case dec:
                return Long.toString(inValue.getValue());
            case hex:
                return Long.toHexString(inValue.getMaskedValue()).toUpperCase();
            case bin:
                return Long.toBinaryString(inValue.getMaskedValue());
            case ascii:
                return "" + (char) inValue.getValue();
            default:
                return inValue.getValueString();
        }
    }
}
