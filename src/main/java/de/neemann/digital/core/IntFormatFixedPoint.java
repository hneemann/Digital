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
        super(parent.getName(), null, IntFormat::decStrLen, parent.isSigned());
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
        final double divisor = (int) Bits.up(1, fixedPoint);
        if (isSigned())
            return new IntFormatFixedPoint(this, inValue -> Double.toString(inValue.getValueSigned() / divisor), fixedPoint);
        else
            return new IntFormatFixedPoint(this, inValue -> Double.toString(inValue.getValue() / divisor), fixedPoint);
    }
}
