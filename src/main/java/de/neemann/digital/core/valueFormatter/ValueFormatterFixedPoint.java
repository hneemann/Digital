/*
 * Copyright (c) 2021 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.valueFormatter;

import de.neemann.digital.core.Bits;
import de.neemann.digital.core.Value;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;

import static de.neemann.digital.core.valueFormatter.ValueFormatterDecimal.decStrLen;

/**
 * Fixed point formatter
 */
public class ValueFormatterFixedPoint implements ValueFormatter {
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
    public ValueFormatterFixedPoint(ElementAttributes attr, boolean signed) {
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

    private String format(Value inValue) {
        if (signed)
            return Double.toString(inValue.getValueSigned() / divisor);
        else
            return Double.toString(inValue.getValue() / divisor);
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
