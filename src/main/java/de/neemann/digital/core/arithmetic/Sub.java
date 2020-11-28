/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.arithmetic;

import de.neemann.digital.core.Bits;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 */
public class Sub extends Add {

    /**
     * The subtractors description
     */
    public static final ElementTypeDescription DESCRIPTION
            = new ElementTypeDescription(Sub.class, input("a"), input("b"), input("c_i"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.BITS)
            .supportsHDL();

    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     */
    public Sub(ElementAttributes attributes) {
        super(attributes);
        getOutputs().get(0).setPinDescription(DESCRIPTION);
        getOutputs().get(1).setPinDescription(DESCRIPTION);
    }

    @Override
    Calc createCalculation(int bits) {
        if (bits < 64) {
            final long mask = Bits.up(1, bits);
            return (a, b, ci, s, co) -> {
                long value = a - b - ci;
                s.setValue(value);
                co.setBool((value & mask) != 0);
            };
        } else {
            return (a, b, ci, s, co) -> {
                long sum = a - b;
                s.setValue(sum - ci);
                co.setBool(subCarry(a, b) | subCarry(sum, ci));
            };
        }
    }

    private static final long LOWMASK = -1L >>> 1;
    private static final long CARRYMASK = 1L << 63;

    private static boolean subCarry(long x, long y) {
        boolean c = (((x & LOWMASK) - (y & LOWMASK)) & CARRYMASK) != 0;
        boolean a = (x & CARRYMASK) != 0;
        boolean b = (y & CARRYMASK) != 0;
        return (!a & b) | ((a == b) & c);
    }

}
