package de.neemann.digital.core.io;


import de.neemann.digital.core.Bits;
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
     * Formats the value.
     * Uses this method to create a string which is only shown to the user.
     * If the user is able to edit the string use {@link IntFormat#formatToEdit(Value)} instead.
     *
     * @param inValue the value to format
     * @return the formatted value as a string
     */
    public String formatToView(Value inValue) {
        if (inValue.isHighZ())
            return "?";

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
                return inValue.toString();
        }
    }

    /**
     * Formats the value.
     * Creates a string which can be parsed by {@link Bits#decode(String)}
     *
     * @param inValue the value to format
     * @return the formatted value as a string
     * @see Bits#decode(String)
     */
    public String formatToEdit(Value inValue) {
        if (inValue.isHighZ())
            return "?";

        switch (this) {
            case dec:
                return Long.toString(inValue.getValue());
            case decSigned:
                return Long.toString(inValue.getValueSigned());
            case bin:
                return "0b" + toBin(inValue);
            case ascii:
                return "'" + (char) inValue.getValue() + "'";
            default:
                return "0x" + toHex(inValue);
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
        long mask = Bits.signedFlagMask(bits);
        for (int i = 0; i < bits; i++) {
            if ((value & mask) != 0)
                sb.append('1');
            else
                sb.append('0');
            mask >>>= 1;
        }
        return sb.toString();
    }
}
