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
     * decimal signed
     */
    decSigned,
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
            case decSigned:
                return Long.toString(inValue.getValueSigned());
            case hex:
                return toHex(inValue);
            case bin:
                return toBin(inValue);
            case ascii:
                return "" + (char) inValue.getValue();
            default:
                return inValue.getValueString();
        }
    }

    private static String toHex(Value inValue) {
        final int bits = inValue.getBits();
        final int numChars = (bits - 1) / 4 + 1;

        StringBuilder sb = new StringBuilder(numChars);
        final long value = inValue.getValue();
        for (int i = numChars - 1; i >= 0; i--) {
            int c = (int) ((value >> (i * 4)) & 0xf);
            sb.append("0123456789ABCDEF".charAt(c));
        }
        return sb.toString();
    }

    private static String toBin(Value inValue) {
        final int bits = inValue.getBits();
        StringBuilder sb = new StringBuilder(bits);
        final long value = inValue.getValue();
        long mask = 1L << (bits - 1);
        for (int i = 0; i < bits; i++) {
            if ((value & mask) != 0)
                sb.append('1');
            else
                sb.append('0');
            mask >>= 1;
        }
        return sb.toString();
    }
}
