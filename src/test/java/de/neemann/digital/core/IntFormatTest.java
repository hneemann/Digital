/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import junit.framework.TestCase;

public class IntFormatTest extends TestCase {

    public void testHex() {
        ValueFormatter vf = IntFormat.HEX_FORMATTER;
        assertEquals("0x1", vf.formatToView(new Value(1, 1)));
        assertEquals("0x1", vf.formatToView(new Value(1, 2)));
        assertEquals("0x1", vf.formatToView(new Value(1, 3)));
        assertEquals("0x1", vf.formatToView(new Value(1, 4)));
        assertEquals("0xF", vf.formatToView(new Value(-1, 4)));
        assertEquals("0x01", vf.formatToView(new Value(1, 5)));
        assertEquals("0x1F", vf.formatToView(new Value(-1, 5)));
        assertEquals("0xFFF", vf.formatToView(new Value(-1, 12)));
        assertEquals("0x1FFF", vf.formatToView(new Value(-1, 13)));
        assertEquals("0x3FFF", vf.formatToView(new Value(-1, 14)));
        assertEquals("0x7FFF", vf.formatToView(new Value(-1, 15)));
        assertEquals("0xFFFF", vf.formatToView(new Value(-1, 16)));
        assertEquals("0xFEDCBA9876543210", vf.formatToView(new Value(0xFEDCBA9876543210L, 64)));
    }

    public void testBin() {
        assertFalse(IntFormat.bin.dependsOnAttributes());
        ValueFormatter vf = IntFormat.bin.createFormatter(null);
        assertEquals("0b1", vf.formatToView(new Value(1, 1)));
        assertEquals("0b01", vf.formatToView(new Value(1, 2)));
        assertEquals("0b001", vf.formatToView(new Value(1, 3)));
        assertEquals("0b111", vf.formatToView(new Value(-1, 3)));
        assertEquals("0b1111111111111111111111111111111111111111111111111111111111111111", vf.formatToView(new Value(-1, 64)));
        assertEquals("0b1000111111111111111111111111111111111111111111111111111111111111", vf.formatToView(new Value(0x8fffffffffffffffL, 64)));
    }

    public void testDec() {
        assertFalse(IntFormat.dec.dependsOnAttributes());
        assertFalse(IntFormat.decSigned.dependsOnAttributes());

        assertEquals("3", IntFormat.dec.createFormatter(null).formatToView(new Value(-1, 2)));
        assertEquals("-1", IntFormat.decSigned.createFormatter(null).formatToView(new Value(-1, 2)));
    }

    public void testDef() {
        ValueFormatter vf = IntFormat.DEFAULT_FORMATTER;
        assertEquals("3", vf.formatToView(new Value(3, 64)));
        assertEquals("0x113", vf.formatToView(new Value(0x113, 64)));
        assertEquals("1A3", vf.formatToView(new Value(0x1A3, 64)));
        assertEquals("FFFFFFFFFFFFFFFF", vf.formatToView(new Value(-1, 64)));
    }

    public void testFixedPoint() {
        ValueFormatter f1 = IntFormat.fixed.createFormatter(new ElementAttributes().set(Keys.FIXED_POINT, 1));
        ValueFormatter f2 = IntFormat.fixed.createFormatter(new ElementAttributes().set(Keys.FIXED_POINT, 2));
        ValueFormatter fs1 = IntFormat.fixedSigned.createFormatter(new ElementAttributes().set(Keys.FIXED_POINT, 1));
        ValueFormatter fs2 = IntFormat.fixedSigned.createFormatter(new ElementAttributes().set(Keys.FIXED_POINT, 2));

        assertEquals("1.5", f1.formatToView(new Value(3, 8)));
        assertEquals("0.75", f2.formatToView(new Value(3, 8)));
        assertEquals("-1.5", fs1.formatToView(new Value(-3, 8)));
        assertEquals("-0.75", fs2.formatToView(new Value(-3, 8)));
        assertEquals("126.5", f1.formatToView(new Value(-3, 8)));
        assertEquals("63.25", f2.formatToView(new Value(-3, 8)));

        assertEquals("1.5:1", f1.formatToEdit(new Value(3, 8)));
        assertEquals("0.75:2", f2.formatToEdit(new Value(3, 8)));
        assertEquals("-1.5:1", fs1.formatToEdit(new Value(-3, 8)));
        assertEquals("-0.75:2", fs2.formatToEdit(new Value(-3, 8)));
        assertEquals("126.5:1", f1.formatToEdit(new Value(-3, 8)));
        assertEquals("63.25:2", f2.formatToEdit(new Value(-3, 8)));

        assertEquals("Z", f1.formatToView(new Value(8)));
        assertEquals("Z", f1.formatToEdit(new Value(8)));
    }

    public void testFloatingPoint() {
        assertFalse(IntFormat.floating.dependsOnAttributes());
        ValueFormatter vf = IntFormat.floating.createFormatter(null);
        Value three32 = new Value(Float.floatToIntBits((float) 3), 32);
        Value three64 = new Value(Double.doubleToLongBits(3), 64);

        assertEquals("3.0", vf.formatToView(three32));
        assertEquals("3.0", vf.formatToView(three64));

        assertEquals("3.0", vf.formatToEdit(three32));
        assertEquals("3.0d", vf.formatToEdit(three64));

        assertEquals("Z", vf.formatToView(new Value(32)));
        assertEquals("Z", vf.formatToEdit(new Value(32)));
        assertEquals("Z", vf.formatToView(new Value(64)));
        assertEquals("Z", vf.formatToEdit(new Value(64)));
    }

    /**
     * Ensures that it is possible to convert a string representation obtained by {@link ValueFormatter#formatToEdit(Value)}
     * back to the same value by {@link Bits#decode(String)}
     */
    public void testBitDecodeConstraint() throws Bits.NumberFormatException {
        for (IntFormat f : IntFormat.values()) {
            if (f.equals(IntFormat.ascii)) {
                checkConstraint(f, tableAscii, false); // ascii supports only 16 bit
            } else if (f.equals(IntFormat.fixed) || f.equals(IntFormat.fixedSigned)) {
                checkConstraintFixedPoint(f, tableFixedPoint);
            } else if (f.equals(IntFormat.floating)) {
                checkConstraint(f, tableFloat, true);
            } else {
                checkConstraint(f, table, false);
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

    private void checkConstraint(IntFormat format, Value[] table, boolean parseFloat) throws Bits.NumberFormatException {
        for (Value val : table) {
            final String str = format.createFormatter(null).formatToEdit(val);
            final Value conv = new Value(Bits.decode(str, parseFloat), val.getBits());
            assertTrue(format.getClass().getSimpleName() + ":" + val + " != " + conv, val.isEqual(conv));
        }
    }

    public void testStrLen() {
        assertFalse(IntFormat.hex.dependsOnAttributes());
        ValueFormatter vf = IntFormat.hex.createFormatter(null);
        assertEquals(6, vf.strLen(16));
        assertEquals(6, vf.strLen(15));
        assertEquals(6, vf.strLen(14));
        assertEquals(6, vf.strLen(13));
        assertEquals(5, vf.strLen(12));

        vf = IntFormat.bin.createFormatter(null);
        assertEquals(18, vf.strLen(16));
        assertEquals(17, vf.strLen(15));
        assertEquals(16, vf.strLen(14));

        vf = IntFormat.dec.createFormatter(null);
        assertEquals(3, vf.strLen(8));
        assertEquals(3, vf.strLen(9));
        assertEquals(4, vf.strLen(10));
        assertEquals(19, vf.strLen(60));
        assertEquals(19, vf.strLen(61));
        assertEquals(19, vf.strLen(62));
        assertEquals(19, vf.strLen(63));
        assertEquals(20, vf.strLen(64));

        vf = IntFormat.decSigned.createFormatter(null);
        assertEquals(4, vf.strLen(8));
        assertEquals(4, vf.strLen(9));
        assertEquals(4, vf.strLen(10));
        assertEquals(5, vf.strLen(11));
        assertEquals(20, vf.strLen(62));
        assertEquals(20, vf.strLen(63));
        assertEquals(20, vf.strLen(64));

        vf = IntFormat.oct.createFormatter(null);
        assertEquals(4, vf.strLen(4));
        assertEquals(4, vf.strLen(5));
        assertEquals(4, vf.strLen(6));
        assertEquals(5, vf.strLen(7));
        assertEquals(5, vf.strLen(8));
        assertEquals(5, vf.strLen(9));
        assertEquals(6, vf.strLen(10));
    }

    public void testDragInt() {
        ValueFormatter f = IntFormat.HEX_FORMATTER;
        checkDragInt(f, 1, 2, 0.004);
        checkDragInt(f, 2, 3, 0.004);
        checkDragInt(f, 1, 11, 0.04);
        checkDragInt(f, 2, 12, 0.04);
        checkDragInt(f, 1, 103, 0.4);
        checkDragInt(f, 2, 104, 0.4);
    }

    public void testDragSigned() {
        assertFalse(IntFormat.decSigned.dependsOnAttributes());
        ValueFormatter f = IntFormat.decSigned.createFormatter(null);
        checkDragInt(f, 1, 2, 0.004);
        checkDragInt(f, 2, 3, 0.004);
        checkDragInt(f, 1, 0, -0.004);
        checkDragInt(f, 2, 1, -0.004);
    }

    private void checkDragInt(ValueFormatter f, long initial, long expected, double inc) {
        assertEquals(expected, f.dragValue(initial, 8, inc));
    }

    public void testDragFloat() {
        assertFalse(IntFormat.floating.dependsOnAttributes());
        ValueFormatter f = IntFormat.floating.createFormatter(null);
        checkDragFloat(f, 1, 1.001, 0.004);
        checkDragFloat(f, 2, 2.002, 0.004);
        checkDragFloat(f, 1, 1.002, 0.04);
        checkDragFloat(f, 2, 2.004, 0.04);
        checkDragFloat(f, 1, 1.4, 0.4);
        checkDragFloat(f, 2, 2.8, 0.4);
        checkDragFloat(f, 1, 3000, 1);
        checkDragFloat(f, 2, 7000, 1);
        checkDragFloat(f, 1, -3000, -1);
        checkDragFloat(f, 2, -7000, -1);
    }

    private void checkDragFloat(ValueFormatter f, double initial, double expected, double inc) {
        long d = f.dragValue(Float.floatToIntBits((float) initial), 32, inc);
        float result = Float.intBitsToFloat((int) d);
        assertEquals((float) expected, result, 1e-5);

        d = f.dragValue(Double.doubleToLongBits(initial), 64, inc);
        double resultd = Double.longBitsToDouble(d);
        assertEquals(expected, resultd, 1e-5);
    }
}
