/*
 * Copyright (c) 2021 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core;

/**
 * Fixed point formatter
 */
public class IntFormatFixedPoint extends IntFormat {
    private static final int[] TABLE = new int[]{
            0, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 14, 15, 16, 17, 17,
            18, 19, 19, 20, 20, 20, 20, 20, 20, 20, 20, 20, 21, 21, 21, 21, 21,
            21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21,
            21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 22};
    private final EditFormat format;
    private final int fixedPoint;

    /**
     * Creates a new generic instance
     *
     * @param name   the name
     * @param signed signed
     */
    IntFormatFixedPoint(String name, boolean signed) {
        super(name, null, null, signed);
        format = null;
        fixedPoint = 0;
    }

    /**
     * Creates a concrete instance
     *
     * @param parent the generic parent
     * @param format the format
     */
    private IntFormatFixedPoint(IntFormatFixedPoint parent, IntFormat.EditFormat format, int fixedPoint) {
        super(parent.getName(), null, bits -> fixedStrLen(bits, fixedPoint), parent.isSigned());
        this.format = format;
        this.fixedPoint = fixedPoint;
    }

    @Override
    public String formatToView(Value inValue) {
        if (inValue.isHighZ())
            return inValue.toString();
        return format.format(inValue);
    }

    @Override
    public String formatToEdit(Value inValue) {
        if (inValue.isHighZ())
            return "Z";
        return format.format(inValue) + ":" + fixedPoint;
    }

    /**
     * Creates a concrete instance
     *
     * @param fixedPoint the number of fractional binary digits
     * @return the concrete instance
     */
    public IntFormat createFixedPoint(int fixedPoint) {
        final double divisor = Bits.up(1, fixedPoint);
        if (isSigned())
            return new IntFormatFixedPoint(this, inValue -> Double.toString(inValue.getValueSigned() / divisor), fixedPoint);
        else
            return new IntFormatFixedPoint(this, inValue -> Double.toString(inValue.getValue() / divisor), fixedPoint);
    }

    private static int fixedStrLen(int bits, int fixedPoint) {
        if (fixedPoint >= TABLE.length) fixedPoint = TABLE.length - 1;
        return decStrLen(Math.max(1, bits - fixedPoint)) + TABLE[fixedPoint];
    }

//    public static void main(String[] args) {
//        for (int fixedPoint = 0; fixedPoint < 64; fixedPoint++) {
//            final double divisor = Bits.up(1, fixedPoint);
//            int max=1;
//            for (int i = 0; i < fixedPoint; i++) {
//                int l = Double.toString(i / divisor).length();
//                if (l>max)
//                    max=l;
//            }
//            System.out.print(max-1+", ");
//        }

}
