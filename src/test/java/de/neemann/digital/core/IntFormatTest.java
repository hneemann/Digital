/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.valueFormatter.*;
import junit.framework.TestCase;

public class IntFormatTest extends TestCase {

    public void testHex() {
        assertEquals("0x1", ValueFormatterHex.INSTANCE.formatToView(new Value(1, 1)));
        assertEquals("0x1", ValueFormatterHex.INSTANCE.formatToView(new Value(1, 2)));
        assertEquals("0x1", ValueFormatterHex.INSTANCE.formatToView(new Value(1, 3)));
        assertEquals("0x1", ValueFormatterHex.INSTANCE.formatToView(new Value(1, 4)));
        assertEquals("0xF", ValueFormatterHex.INSTANCE.formatToView(new Value(-1, 4)));
        assertEquals("0x01", ValueFormatterHex.INSTANCE.formatToView(new Value(1, 5)));
        assertEquals("0x1F", ValueFormatterHex.INSTANCE.formatToView(new Value(-1, 5)));
        assertEquals("0xFFF", ValueFormatterHex.INSTANCE.formatToView(new Value(-1, 12)));
        assertEquals("0x1FFF", ValueFormatterHex.INSTANCE.formatToView(new Value(-1, 13)));
        assertEquals("0x3FFF", ValueFormatterHex.INSTANCE.formatToView(new Value(-1, 14)));
        assertEquals("0x7FFF", ValueFormatterHex.INSTANCE.formatToView(new Value(-1, 15)));
        assertEquals("0xFFFF", ValueFormatterHex.INSTANCE.formatToView(new Value(-1, 16)));
        assertEquals("0xFEDCBA9876543210", ValueFormatterHex.INSTANCE.formatToView(new Value(0xFEDCBA9876543210L, 64)));
    }

    public void testBin() {
        assertEquals("0b1", ValueFormatterBinary.INSTANCE.formatToView(new Value(1, 1)));
        assertEquals("0b01", ValueFormatterBinary.INSTANCE.formatToView(new Value(1, 2)));
        assertEquals("0b001", ValueFormatterBinary.INSTANCE.formatToView(new Value(1, 3)));
        assertEquals("0b111", ValueFormatterBinary.INSTANCE.formatToView(new Value(-1, 3)));
        assertEquals("0b1111111111111111111111111111111111111111111111111111111111111111", ValueFormatterBinary.INSTANCE.formatToView(new Value(-1, 64)));
        assertEquals("0b1000111111111111111111111111111111111111111111111111111111111111", ValueFormatterBinary.INSTANCE.formatToView(new Value(0x8fffffffffffffffL, 64)));
    }

    public void testDec() {
        assertEquals("3", ValueFormatterDecimal.UNSIGNED.formatToView(new Value(-1, 2)));
        assertEquals("-1", ValueFormatterDecimal.SIGNED.formatToView(new Value(-1, 2)));
    }

    public void testDef() {
        assertEquals("3", ValueFormatterDefault.INSTANCE.formatToView(new Value(3, 64)));
        assertEquals("0x113", ValueFormatterDefault.INSTANCE.formatToView(new Value(0x113, 64)));
        assertEquals("1A3", ValueFormatterDefault.INSTANCE.formatToView(new Value(0x1A3, 64)));
        assertEquals("FFFFFFFFFFFFFFFF", ValueFormatterDefault.INSTANCE.formatToView(new Value(-1, 64)));
    }

    /**
     * Ensures that it is possible to convert a string representation obtained by {@link ValueFormatter#formatToEdit(Value)}
     * back to the same value by {@link Bits#decode(String)}
     */
    public void testBitDecodeConstraint() throws Bits.NumberFormatException {
        for (IntFormat f : IntFormat.values()) {
            if (f.equals(IntFormat.ascii)) {
                checkConstraint(f, tableAscii); // ascii supports only 16 bit
            } else if (f.equals(IntFormat.fixed) || f.equals(IntFormat.fixedSigned)) {
                checkConstraintFixedPoint(f, tableFixedPoint);
            } else if (f.equals(IntFormat.floating)) {
                checkConstraint(f, tableFloat);
            } else {
                checkConstraint(f, table);
            }
        }
    }

    private void checkConstraintFixedPoint(IntFormat f, Value[] table) throws Bits.NumberFormatException {
        for (int digits = 1; digits < 5; digits++) {
            ValueFormatter format = f.createFormatter(new ElementAttributes().set(Keys.FIXED_POINT, digits));
            for (Value val : table) {
                final String str = format.formatToEdit(val);
                final Value conv = new Value(Bits.decode(str), val.getBits());
                assertTrue(f.name() + ":" + val + " != " + conv, val.isEqual(conv));
            }
        }
    }

    private static final Value[] table = new Value[]{
            new Value(1, 2),
            new Value(-1, 2),
            new Value(1, 64),
            new Value(10, 8),
            new Value(17, 8),
            new Value(-1, 64),
            new Value(0x4fffffffffffffffL, 63),
            new Value(0x8fffffffffffffffL, 64),
    };

    private static final Value[] tableFixedPoint = new Value[]{
            new Value(1, 2),
            new Value(-1, 2),
            new Value(1, 64),
            new Value(10, 8),
            new Value(17, 8),
            new Value(-1, 64),
    };

    private static final Value[] tableFloat = new Value[]{
            new Value(1, 2),
            new Value(Float.floatToIntBits(-1), 32),
            new Value(Float.floatToIntBits(1.2f), 32),
            new Value(Double.doubleToLongBits(-1), 64),
            new Value(Double.doubleToLongBits(1.2f), 64),
    };

    private static final Value[] tableAscii = new Value[]{
            new Value(65, 8),
            new Value(66, 8),
            new Value(1000, 16),
            new Value(-1, 7),
            new Value(-1, 7),
    };

    private void checkConstraint(IntFormat format, Value[] table) throws Bits.NumberFormatException {
        for (Value val : table) {
            final String str = format.createFormatter(null).formatToEdit(val);
            final Value conv = new Value(Bits.decode(str), val.getBits());
            assertTrue(format.getClass().getSimpleName() + ":" + val + " != " + conv, val.isEqual(conv));
        }
    }

    public void testStrLen() {
        assertEquals(6, ValueFormatterHex.INSTANCE.strLen(16));
        assertEquals(6, ValueFormatterHex.INSTANCE.strLen(15));
        assertEquals(6, ValueFormatterHex.INSTANCE.strLen(14));
        assertEquals(6, ValueFormatterHex.INSTANCE.strLen(13));
        assertEquals(5, ValueFormatterHex.INSTANCE.strLen(12));

        assertEquals(18, ValueFormatterBinary.INSTANCE.strLen(16));
        assertEquals(17, ValueFormatterBinary.INSTANCE.strLen(15));
        assertEquals(16, ValueFormatterBinary.INSTANCE.strLen(14));

        assertEquals(3, ValueFormatterDecimal.UNSIGNED.strLen(8));
        assertEquals(3, ValueFormatterDecimal.UNSIGNED.strLen(9));
        assertEquals(4, ValueFormatterDecimal.UNSIGNED.strLen(10));
        assertEquals(19, ValueFormatterDecimal.UNSIGNED.strLen(60));
        assertEquals(19, ValueFormatterDecimal.UNSIGNED.strLen(61));
        assertEquals(19, ValueFormatterDecimal.UNSIGNED.strLen(62));
        assertEquals(19, ValueFormatterDecimal.UNSIGNED.strLen(63));
        assertEquals(20, ValueFormatterDecimal.UNSIGNED.strLen(64));

        assertEquals(4, ValueFormatterDecimal.SIGNED.strLen(8));
        assertEquals(4, ValueFormatterDecimal.SIGNED.strLen(9));
        assertEquals(4, ValueFormatterDecimal.SIGNED.strLen(10));
        assertEquals(5, ValueFormatterDecimal.SIGNED.strLen(11));
        assertEquals(20, ValueFormatterDecimal.SIGNED.strLen(62));
        assertEquals(20, ValueFormatterDecimal.SIGNED.strLen(63));
        assertEquals(20, ValueFormatterDecimal.SIGNED.strLen(64));

        assertEquals(4, ValueFormatterOctal.INSTANCE.strLen(4));
        assertEquals(4, ValueFormatterOctal.INSTANCE.strLen(5));
        assertEquals(4, ValueFormatterOctal.INSTANCE.strLen(6));
        assertEquals(5, ValueFormatterOctal.INSTANCE.strLen(7));
        assertEquals(5, ValueFormatterOctal.INSTANCE.strLen(8));
        assertEquals(5, ValueFormatterOctal.INSTANCE.strLen(9));
        assertEquals(6, ValueFormatterOctal.INSTANCE.strLen(10));

    }
}
