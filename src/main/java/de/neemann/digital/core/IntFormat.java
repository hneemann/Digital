/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.valueFormatter.*;

/**
 * The available number formats
 */
public enum IntFormat {
    /**
     * the default format
     */
    def(attributes -> ValueFormatterDefault.INSTANCE, false),
    /**
     * decimal
     */
    dec(attributes -> ValueFormatterDecimal.UNSIGNED, false),
    /**
     * decimal signed
     */
    decSigned(attributes -> ValueFormatterDecimal.SIGNED, true),
    /**
     * hex
     */
    hex(attributes -> ValueFormatterHex.INSTANCE, false),
    /**
     * binary
     */
    bin(attributes -> ValueFormatterBinary.INSTANCE, false),
    /**
     * octal
     */
    oct(attributes -> ValueFormatterOctal.INSTANCE, false),
    /**
     * ascii
     */
    ascii(attributes -> ValueFormatterAscii.INSTANCE, false),
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
    floating(attributes -> ValueFormatterFloat.INSTANCE, true);

    private final Factory factory;
    private final boolean signed;

    IntFormat(Factory factory, boolean signed) {
        this.factory = factory;
        this.signed = signed;
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


    private interface Factory {
        ValueFormatter create(ElementAttributes attributes);
    }
}
