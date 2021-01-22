/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core;

import junit.framework.TestCase;

public class IntFormatTest extends TestCase {

    public void testHex() throws Exception {
        assertEquals("0x1", IntFormat.HEX.formatToView(new Value(1, 1)));
        assertEquals("0x1", IntFormat.HEX.formatToView(new Value(1, 2)));
        assertEquals("0x1", IntFormat.HEX.formatToView(new Value(1, 3)));
        assertEquals("0x1", IntFormat.HEX.formatToView(new Value(1, 4)));
        assertEquals("0xF", IntFormat.HEX.formatToView(new Value(-1, 4)));
        assertEquals("0x01", IntFormat.HEX.formatToView(new Value(1, 5)));
        assertEquals("0x1F", IntFormat.HEX.formatToView(new Value(-1, 5)));
        assertEquals("0xFFF", IntFormat.HEX.formatToView(new Value(-1, 12)));
        assertEquals("0x1FFF", IntFormat.HEX.formatToView(new Value(-1, 13)));
        assertEquals("0x3FFF", IntFormat.HEX.formatToView(new Value(-1, 14)));
        assertEquals("0x7FFF", IntFormat.HEX.formatToView(new Value(-1, 15)));
        assertEquals("0xFFFF", IntFormat.HEX.formatToView(new Value(-1, 16)));
        assertEquals("0xFEDCBA9876543210", IntFormat.HEX.formatToView(new Value(0xFEDCBA9876543210L, 64)));
    }

    public void testBin() {
        assertEquals("0b1", IntFormat.BIN.formatToView(new Value(1, 1)));
        assertEquals("0b01", IntFormat.BIN.formatToView(new Value(1, 2)));
        assertEquals("0b001", IntFormat.BIN.formatToView(new Value(1, 3)));
        assertEquals("0b111", IntFormat.BIN.formatToView(new Value(-1, 3)));
        assertEquals("0b1111111111111111111111111111111111111111111111111111111111111111", IntFormat.BIN.formatToView(new Value(-1, 64)));
        assertEquals("0b1000111111111111111111111111111111111111111111111111111111111111", IntFormat.BIN.formatToView(new Value(0x8fffffffffffffffL, 64)));
    }

    public void testDec() throws Exception {
        assertEquals("3", IntFormat.DEC.formatToView(new Value(-1, 2)));
        assertEquals("-1", IntFormat.DEC_SIGNED.formatToView(new Value(-1, 2)));
    }

    public void testDef() throws Exception {
        assertEquals("3", IntFormat.DEF.formatToView(new Value(3, 64)));
        assertEquals("0x113", IntFormat.DEF.formatToView(new Value(0x113, 64)));
        assertEquals("1A3", IntFormat.DEF.formatToView(new Value(0x1A3, 64)));
        assertEquals("FFFFFFFFFFFFFFFF", IntFormat.DEF.formatToView(new Value(-1, 64)));
    }

    /**
     * Ensures that it is possible to convert a string representation obtained by {@link IntFormat#formatToEdit(Value)}
     * back to the same value by {@link Bits#decode(String)}
     */
    public void testBitDecodeConstraint() throws Bits.NumberFormatException {
        for (IntFormat f : IntFormat.VALUES) {
            if (f == IntFormat.ASCII) {
                checkConstraint(f, tableAscii); // ascii supports only 16 bit
            } else {
                checkConstraint(f, table);
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

    private static final Value[] tableAscii = new Value[]{
            new Value(65, 8),
            new Value(65, 8),
            new Value(1000, 16),
            new Value(-1, 7),
            new Value(-1, 7),
    };

    private void checkConstraint(IntFormat format, Value[] table) throws Bits.NumberFormatException {
        for (Value val : table) {
            final String str = format.formatToEdit(val);
            final Value conv = new Value(Bits.decode(str), val.getBits());
            assertTrue(format.getName() + ":" + val + " != " + conv, val.isEqual(conv));
        }
    }

    public void testStrLen() {
        assertEquals(6, IntFormat.HEX.strLen(16));
        assertEquals(6, IntFormat.HEX.strLen(15));
        assertEquals(6, IntFormat.HEX.strLen(14));
        assertEquals(6, IntFormat.HEX.strLen(13));
        assertEquals(5, IntFormat.HEX.strLen(12));

        assertEquals(18, IntFormat.BIN.strLen(16));
        assertEquals(17, IntFormat.BIN.strLen(15));
        assertEquals(16, IntFormat.BIN.strLen(14));

        assertEquals(3, IntFormat.DEC.strLen(8));
        assertEquals(3, IntFormat.DEC.strLen(9));
        assertEquals(4, IntFormat.DEC.strLen(10));
        assertEquals(19, IntFormat.DEC.strLen(60));
        assertEquals(19, IntFormat.DEC.strLen(61));
        assertEquals(19, IntFormat.DEC.strLen(62));
        assertEquals(19, IntFormat.DEC.strLen(63));
        assertEquals(20, IntFormat.DEC.strLen(64));

        assertEquals(4, IntFormat.DEC_SIGNED.strLen(8));
        assertEquals(4, IntFormat.DEC_SIGNED.strLen(9));
        assertEquals(4, IntFormat.DEC_SIGNED.strLen(10));
        assertEquals(5, IntFormat.DEC_SIGNED.strLen(11));
        assertEquals(20, IntFormat.DEC_SIGNED.strLen(62));
        assertEquals(20, IntFormat.DEC_SIGNED.strLen(63));
        assertEquals(20, IntFormat.DEC_SIGNED.strLen(64));

        assertEquals(4, IntFormat.OCT.strLen(4));
        assertEquals(4, IntFormat.OCT.strLen(5));
        assertEquals(4, IntFormat.OCT.strLen(6));
        assertEquals(5, IntFormat.OCT.strLen(7));
        assertEquals(5, IntFormat.OCT.strLen(8));
        assertEquals(5, IntFormat.OCT.strLen(9));
        assertEquals(6, IntFormat.OCT.strLen(10));

    }
}
