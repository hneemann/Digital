/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;

/**
 * The available number formats
 */
public enum IntFormat {
    /**
     * the default format
     */
    def(ValueFormatterDefault.INSTANCE, false),
    /**
     * decimal
     */
    dec(new ValueFormatterDecimal(false), false),
    /**
     * decimal signed
     */
    decSigned(new ValueFormatterDecimal(true), true),
    /**
     * hex
     */
    hex(ValueFormatterHex.INSTANCE, false),
    /**
     * binary
     */
    bin(new ValueFormatterBinary(), false),
    /**
     * octal
     */
    oct(new ValueFormatterOctal(), false),
    /**
     * ascii
     */
    ascii(new ValueFormatterAscii(), false),
    /**
     * fixed point
     */
    fixed(attributes -> new ValueFormatterFixedPoint(attributes, false), false),
    /**
     * fixed point signed
     */
    fixedSigned(attributes -> new ValueFormatterFixedPoint(attributes, true), true),
    /**
     * floating point
     */
    floating(new ValueFormatterFloat(), true);

    /**
     * The default formatter
     */
    public static final ValueFormatter DEFAULT_FORMATTER = ValueFormatterDefault.INSTANCE;
    /**
     * The hexadecimal formatter
     */
    public static final ValueFormatter HEX_FORMATTER = ValueFormatterHex.INSTANCE;

    private final Factory factory;
    private final boolean signed;
    private final boolean dependsOnAttributes;

    IntFormat(ValueFormatter instance, boolean signed) {
        this(attributes -> instance, signed, false);
    }

    IntFormat(Factory factory, boolean signed) {
        this(factory, signed, true);
    }

    IntFormat(Factory factory, boolean signed, boolean dependsOnAttributes) {
        this.factory = factory;
        this.signed = signed;
        this.dependsOnAttributes = dependsOnAttributes;
    }

    /**
     * Creates a formatter which is able to format Values
     *
     * @param attributes the elements attributes
     * @return the created {@link ValueFormatter}
     */
    public ValueFormatter createFormatter(ElementAttributes attributes) {
        return factory.create(attributes);
    }

    /**
     * @return true if this formatter takes the sign of the value into account
     */
    public boolean isSigned() {
        return signed;
    }

    /**
     * @return true if this type depends on elements attributes
     */
    public boolean dependsOnAttributes() {
        return dependsOnAttributes;
    }

    private interface Factory {
        ValueFormatter create(ElementAttributes attributes);
    }

    private static final char[] DIGITS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    /**
     * The default value formatter
     */
    private static final class ValueFormatterDefault implements ValueFormatter {
        private static final ValueFormatter INSTANCE = new ValueFormatterDefault();

        @Override
        public String formatToView(Value inValue) {
            if (inValue.isHighZ())
                return inValue.toString();
            else
                return toShortHex(inValue.getValue(), false);
        }

        @Override
        public String formatToEdit(Value inValue) {
            if (inValue.isHighZ())
                return "Z";

            final long value = inValue.getValue();
            if (value >= 0 && value < 10)
                return Long.toString(value);
            else
                return "0x" + toShortHex(value, true);
        }

        @Override
        public int strLen(int bits) {
            return (bits - 1) / 4 + 3;
        }

        @Override
        public boolean isSuitedForAddresses() {
            return false; // difficult to read in a table
        }
    }

    /**
     * Base class of all formatters where the string to edit and the string to display are the same.
     */
    private static abstract class ValueFormatterViewEdit implements ValueFormatter {
        private final boolean suitedForAddresses;

        private ValueFormatterViewEdit(boolean suitedForAddresses) {
            this.suitedForAddresses = suitedForAddresses;
        }

        @Override
        public String formatToView(Value inValue) {
            if (inValue.isHighZ())
                return inValue.toString();
            else
                return format(inValue);
        }

        @Override
        public String formatToEdit(Value inValue) {
            if (inValue.isHighZ())
                return "Z";
            else
                return format(inValue);
        }

        @Override
        public boolean isSuitedForAddresses() {
            return suitedForAddresses;
        }

        protected abstract String format(Value value);
    }

    /**
     * the hexadecimal formatter
     */
    private static final class ValueFormatterHex extends ValueFormatterViewEdit {
        private static final ValueFormatterHex INSTANCE = new ValueFormatterHex();

        private ValueFormatterHex() {
            super(true);
        }

        @Override
        protected String format(Value inValue) {
            final int bits = inValue.getBits();
            final int numChars = (bits - 1) / 4 + 1;

            StringBuilder sb = new StringBuilder("0x");
            final long value = inValue.getValue();
            for (int i = numChars - 1; i >= 0; i--) {
                int c = (int) ((value >> (i * 4)) & 0xf);
                sb.append(DIGITS[c]);
            }
            return sb.toString();
        }

        @Override
        public int strLen(int bits) {
            return (bits - 1) / 4 + 3;
        }
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

    /**
     * the octal formatter
     */
    private static final class ValueFormatterOctal extends ValueFormatterViewEdit {

        private ValueFormatterOctal() {
            super(true);
        }

        @Override
        public int strLen(int bits) {
            return (bits - 1) / 3 + 3;
        }

        @Override
        protected String format(Value inValue) {
            final int bits = inValue.getBits();
            final int numChars = (bits - 1) / 3 + 1;

            StringBuilder sb = new StringBuilder("0");
            final long value = inValue.getValue();
            for (int i = numChars - 1; i >= 0; i--) {
                int c = (int) ((value >> (i * 3)) & 0x7);
                sb.append(DIGITS[c]);
            }
            return sb.toString();
        }
    }

    /**
     * the binary formatter
     */
    private static final class ValueFormatterBinary extends ValueFormatterViewEdit {

        private ValueFormatterBinary() {
            super(false); // column becomes to wide
        }

        @Override
        public int strLen(int bits) {
            return bits + 2;
        }

        @Override
        protected String format(Value inValue) {
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
            return "0b" + new String(data);
        }
    }

    /**
     * The ascii formatter
     */
    private static final class ValueFormatterAscii extends ValueFormatterViewEdit {

        private ValueFormatterAscii() {
            super(false); // does not represent all values
        }

        @Override
        public int strLen(int bits) {
            return 3;
        }

        @Override
        protected String format(Value value) {
            return "'" + ((char) value.getValue()) + "'";
        }
    }

    /**
     * The decimal value formatter
     */
    private static final class ValueFormatterDecimal extends ValueFormatterViewEdit {
        private final boolean signed;

        private ValueFormatterDecimal(boolean signed) {
            super(true);
            this.signed = signed;
        }

        @Override
        public int strLen(int bits) {
            if (signed)
                return decStrLen(bits - 1) + 1;
            else
                return decStrLen(bits);
        }

        @Override
        protected String format(Value value) {
            if (signed)
                return Long.toString(value.getValueSigned());
            else
                return Long.toString(value.getValue());
        }
    }

    private static int decStrLen(int bits) {
        if (bits == 64)
            return 20;
        else if (bits == 63) {
            return 19;
        } else
            return (int) Math.ceil(Math.log10(1L << bits));
    }


    /**
     * Fixed point formatter
     */
    private static final class ValueFormatterFixedPoint implements ValueFormatter {
        private static final int[] TABLE = new int[]{
                0, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 14, 15, 16, 17, 17,
                18, 19, 19, 20, 20, 20, 20, 20, 20, 20, 20, 20, 21, 21, 21, 21, 21,
                21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21,
                21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 22};

        private final int fixedPoint;
        private final boolean signed;
        private final double divisor;

        /**
         * Creates a new generic instance
         *
         * @param attr   the defining elements attributes
         * @param signed signed
         */
        private ValueFormatterFixedPoint(ElementAttributes attr, boolean signed) {
            fixedPoint = attr.get(Keys.FIXED_POINT);
            divisor = Bits.up(1, fixedPoint);
            this.signed = signed;
        }

        @Override
        public String formatToView(Value inValue) {
            if (inValue.isHighZ())
                return inValue.toString();
            return format(inValue);
        }

        @Override
        public String formatToEdit(Value inValue) {
            if (inValue.isHighZ())
                return "Z";
            return format(inValue) + ":" + fixedPoint;
        }

        @Override
        public int strLen(int bits) {
            int fp = fixedPoint;
            if (fp >= TABLE.length) fp = TABLE.length - 1;
            return decStrLen(Math.max(1, bits - fp)) + TABLE[fp];
        }

        @Override
        public boolean isSuitedForAddresses() {
            return false;
        }

        private String format(Value inValue) {
            if (signed)
                return Double.toString(inValue.getValueSigned() / divisor);
            else
                return Double.toString(inValue.getValue() / divisor);
        }
    }

    /**
     * Floating point formatter
     */
    private static final class ValueFormatterFloat implements ValueFormatter {
        private static final int SIZE32 = Float.toString((float) -Math.PI).length();
        private static final int SIZE64 = Double.toString(-Math.PI).length();

        @Override
        public String formatToView(Value inValue) {
            if (inValue.isHighZ())
                return inValue.toString();

            switch (inValue.getBits()) {
                case 32:
                    return Float.toString(Float.intBitsToFloat((int) inValue.getValue()));
                case 64:
                    return Double.toString(Double.longBitsToDouble(inValue.getValue()));
                default:
                    return HEX_FORMATTER.formatToView(inValue);
            }
        }

        @Override
        public String formatToEdit(Value inValue) {
            if (inValue.isHighZ())
                return "Z";

            switch (inValue.getBits()) {
                case 32:
                    return Float.toString(Float.intBitsToFloat((int) inValue.getValue()));
                case 64:
                    return Double.longBitsToDouble(inValue.getValue()) + "d";
                default:
                    return HEX_FORMATTER.formatToEdit(inValue);
            }
        }

        @Override
        public int strLen(int bits) {
            switch (bits) {
                case 32:
                    return SIZE32;
                case 64:
                    return SIZE64;
                default:
                    return HEX_FORMATTER.strLen(bits);
            }
        }

        @Override
        public boolean isSuitedForAddresses() {
            return false;
        }
    }

}
