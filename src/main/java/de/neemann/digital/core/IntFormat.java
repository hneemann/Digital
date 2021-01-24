/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.valueFormatter.*;

import static de.neemann.digital.core.valueFormatter.ValueFormatterDefault.DIGITS;

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
            super(false); // becomes to large
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
         * @param attr   the definig elements attributes
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
                    return ValueFormatterHex.INSTANCE.formatToView(inValue);
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
                    return ValueFormatterHex.INSTANCE.formatToEdit(inValue);
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
                    return ValueFormatterHex.INSTANCE.strLen(bits);
            }
        }

        @Override
        public boolean isSuitedForAddresses() {
            return false;
        }
    }

}
