/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core;

import java.util.Objects;

/**
 * The int format used to format numbers
 */
public class IntFormat {
    /**
     * the default format
     */
    public static final IntFormat DEF = new IntFormat("def", v -> {
        final long value = v.getValue();
        if (value >= 0 && value < 10)
            return Long.toString(value);
        else
            return "0x" + toShortHex(value, true);
    }, bits -> (bits - 1) / 4 + 3) {
        @Override
        public String formatToView(Value inValue) {
            if (inValue.isHighZ())
                return inValue.toString();
            else
                return toShortHex(inValue.getValue(), false);
        }
    };

    /**
     * decimal
     */
    public static final IntFormat DEC = new IntFormat("dec", v -> Long.toString(v.getValue()), IntFormat::decStrLen);
    /**
     * decimal signed
     */
    public static final IntFormat DEC_SIGNED = new IntFormat("decSigned", v -> Long.toString(v.getValueSigned()), bits -> decStrLen(bits - 1) + 1, true);
    /**
     * hexadecimal
     */
    public static final IntFormat HEX = new IntFormat("hex", v -> "0x" + toHex(v), bits -> (bits - 1) / 4 + 3);
    /**
     * binary
     */
    public static final IntFormat BIN = new IntFormat("bin", v -> "0b" + toBin(v), bits -> bits + 2);
    /**
     * octal
     */
    public static final IntFormat OCT = new IntFormat("oct", v -> "0" + toOct(v), bits -> (bits - 1) / 3 + 3);
    /**
     * ascii format
     */
    public static final IntFormat ASCII = new IntFormat("ascii", v -> "'" + (char) v.getValue() + "'", bits -> 3);
    /**
     * fixed point format
     */
    public static final IntFormat FIXED_POINT = new IntFormatFixedPoint("fixed", false);
    /**
     * signed fixed point format
     */
    public static final IntFormat FIXED_POINT_SIGNED = new IntFormatFixedPoint("fixedSigned", true);
    /**
     * float format
     */
    public static final IntFormat FLOAT = new IntFormatFloat();

    /**
     * All the available formats
     */
    public static final IntFormat[] VALUES = new IntFormat[]{DEF, DEC, DEC_SIGNED, HEX, BIN, OCT, ASCII, FIXED_POINT, FIXED_POINT_SIGNED, FLOAT};

    private final String name;
    private final EditFormat format;
    private final StrLen strLen;
    private final boolean signed;

    IntFormat(String name, EditFormat format, StrLen strLen) {
        this(name, format, strLen, false);
    }

    IntFormat(String name, EditFormat format, StrLen strLen, boolean signed) {
        this.name = name;
        this.format = format;
        this.strLen = strLen;
        this.signed = signed;
    }

    /**
     * Returns the format instance matching th given name
     *
     * @param name the name of the format
     * @return the format
     */
    public static IntFormat getFromName(String name) {
        for (IntFormat f : VALUES)
            if (f.getName().equals(name))
                return f;
        return DEF;
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
        else
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

    /**
     * @return the name of this format
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }

    static int decStrLen(int bits) {
        if (bits == 64)
            return 20;
        else if (bits == 63) {
            return 19;
        } else
            return (int) Math.ceil(Math.log10(1L << bits));
    }

    private static final char[] DIGITS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    static String toHex(Value inValue) {
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

    static String toShortHex(long value, boolean omitPrefix) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntFormat intFormat = (IntFormat) o;
        return name.equals(intFormat.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    interface StrLen {
        int strLen(int bits);
    }

    interface EditFormat {
        String format(Value inValue);
    }
}
