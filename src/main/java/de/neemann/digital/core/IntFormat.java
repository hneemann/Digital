/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core;

/**
 * The int format used to format numbers
 */
public enum IntFormat {
    /**
     * the default format
     */
    def(v -> {
        final long value = v.getValue();
        if (value >= 0 && value < 10)
            return Long.toString(value);
        else
            return "0x" + toShortHex(value, true);
    }, bits -> (bits - 1) / 4 + 3),
    /**
     * decimal
     */
    dec(v -> Long.toString(v.getValue()), IntFormat::decStrLen),
    /**
     * decimal signed
     */
    decSigned(v -> Long.toString(v.getValueSigned()), bits -> decStrLen(bits - 1) + 1, true),
    /**
     * hexadecimal
     */
    hex(v -> "0x" + toHex(v), bits -> (bits - 1) / 4 + 3),
    /**
     * binary
     */
    bin(v -> "0b" + toBin(v), bits -> bits + 2),
    /**
     * octal
     */
    oct(v -> "0" + toOct(v), bits -> (bits - 1) / 3 + 3),
    /**
     * ascii format
     */
    ascii(v -> "'" + (char) v.getValue() + "'", bits -> 3);


    private final EditFormat format;
    private final StrLen strLen;
    private final boolean signed;

    IntFormat(EditFormat format, StrLen strLen) {
        this(format, strLen, false);
    }

    IntFormat(EditFormat format, StrLen strLen, boolean signed) {
        this.format = format;
        this.strLen = strLen;
        this.signed = signed;
    }

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
            return inValue.toString();

        if (this.equals(def))
            return toShortHex(inValue.getValue(), false);

        return formatToEdit(inValue);
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
            return "Z";

        return format.format(inValue);
    }

    /**
     * Return the number of characters required to format a number with the given bit width.
     *
     * @param bits the number of bits
     * @return the number of characters required
     */
    public int strLen(int bits) {
        return strLen.strLen(bits);
    }

    private static int decStrLen(int bits) {
        if (bits == 64)
            return 20;
        else if (bits == 63) {
            return 19;
        } else
            return (int) Math.ceil(Math.log10(1L << bits));
    }

    private static final char[] DIGITS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private static String toHex(Value inValue) {
        final int bits = inValue.getBits();
        final int numChars = (bits - 1) / 4 + 1;

        StringBuilder sb = new StringBuilder(numChars);
        final long value = inValue.getValue();
        for (int i = numChars - 1; i >= 0; i--) {
            int c = (int) ((value >> (i * 4)) & 0xf);
            sb.append(DIGITS[c]);
        }
        return sb.toString();
    }

    private static String toOct(Value inValue) {
        final int bits = inValue.getBits();
        final int numChars = (bits - 1) / 3 + 1;

        StringBuilder sb = new StringBuilder(numChars);
        final long value = inValue.getValue();
        for (int i = numChars - 1; i >= 0; i--) {
            int c = (int) ((value >> (i * 3)) & 0x7);
            sb.append(DIGITS[c]);
        }
        return sb.toString();
    }


    /**
     * Creates a short hex representation of the given value.
     * Use only to represent a value.
     * If confusion is excluded, the prefix '0x' is omitted.
     * Thus 0x1A3 is converted to "1A3" which can not be parsed back to a long because "0x" is missing.
     *
     * @param value the value
     * @return the hex string
     */
    public static String toShortHex(long value) {
        return toShortHex(value, false);
    }

    private static final int BUF = 16;

    private static String toShortHex(long value, boolean omitPrefix) {
        if (value == 0)
            return "0";

        boolean wasChar = false;
        int p = BUF;
        char[] data = new char[BUF];
        while (value != 0) {
            final int d = (int) (value & 0xf);
            if (d >= 10) wasChar = true;
            p--;
            data[p] = DIGITS[d];
            value >>>= 4;
        }

        if (omitPrefix || wasChar || p == BUF - 1)
            return new String(data, p, BUF - p);
        else
            return "0x" + new String(data, p, BUF - p);
    }

    private static String toBin(Value inValue) {
        final int bits = inValue.getBits();
        char[] data = new char[bits];
        final long value = inValue.getValue();
        long mask = 1;
        for (int i = bits - 1; i >= 0; i--) {
            if ((value & mask) != 0)
                data[i] = '1';
            else
                data[i] = '0';
            mask <<= 1;
        }
        return new String(data);
    }

    /**
     * @return true if the format supports signed values
     */
    public boolean isSigned() {
        return signed;
    }

    private interface StrLen {
        int strLen(int bits);
    }

    private interface EditFormat {
        String format(Value inValue);
    }
}
