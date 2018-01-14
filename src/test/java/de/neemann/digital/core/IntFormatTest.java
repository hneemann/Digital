package de.neemann.digital.core;

import junit.framework.TestCase;

public class IntFormatTest extends TestCase {

    public void testHex() throws Exception {
        assertEquals("1", IntFormat.hex.formatToView(new Value(1, 1)));
        assertEquals("1", IntFormat.hex.formatToView(new Value(1, 2)));
        assertEquals("1", IntFormat.hex.formatToView(new Value(1, 3)));
        assertEquals("1", IntFormat.hex.formatToView(new Value(1, 4)));
        assertEquals("F", IntFormat.hex.formatToView(new Value(-1, 4)));
        assertEquals("01", IntFormat.hex.formatToView(new Value(1, 5)));
        assertEquals("1F", IntFormat.hex.formatToView(new Value(-1, 5)));
        assertEquals("FFF", IntFormat.hex.formatToView(new Value(-1, 12)));
        assertEquals("1FFF", IntFormat.hex.formatToView(new Value(-1, 13)));
        assertEquals("3FFF", IntFormat.hex.formatToView(new Value(-1, 14)));
        assertEquals("7FFF", IntFormat.hex.formatToView(new Value(-1, 15)));
        assertEquals("FFFF", IntFormat.hex.formatToView(new Value(-1, 16)));
        assertEquals("FEDCBA9876543210", IntFormat.hex.formatToView(new Value(0xFEDCBA9876543210L, 64)));
    }

    public void testBin() {
        assertEquals("1", IntFormat.bin.formatToView(new Value(1, 1)));
        assertEquals("01", IntFormat.bin.formatToView(new Value(1, 2)));
        assertEquals("001", IntFormat.bin.formatToView(new Value(1, 3)));
        assertEquals("111", IntFormat.bin.formatToView(new Value(-1, 3)));
        assertEquals("1111111111111111111111111111111111111111111111111111111111111111", IntFormat.bin.formatToView(new Value(-1, 64)));
        assertEquals("1000111111111111111111111111111111111111111111111111111111111111", IntFormat.bin.formatToView(new Value(0x8fffffffffffffffL, 64)));
    }

    public void testDec() throws Exception {
        assertEquals("3", IntFormat.dec.formatToView(new Value(-1, 2)));
        assertEquals("-1", IntFormat.decSigned.formatToView(new Value(-1, 2)));
    }

    public void testDef() throws Exception {
        assertEquals("3", IntFormat.def.formatToView(new Value(3, 64)));
        assertEquals("0x113", IntFormat.def.formatToView(new Value(0x113, 64)));
        assertEquals("1A3", IntFormat.def.formatToView(new Value(0x1A3, 64)));
        assertEquals("FFFFFFFFFFFFFFFF", IntFormat.def.formatToView(new Value(-1, 64)));
    }

    /**
     * Ensures that it is possible to convert a string representation obtained by {@link IntFormat#formatToEdit(Value)}
     * back to the same value by {@link Bits#decode(String)}
     */
    public void testBitDecodeConstraint() throws Bits.NumberFormatException {
        for (IntFormat f : IntFormat.values()) {
            if (f == IntFormat.ascii) {
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
            assertTrue(format.name() + ":" + val + " != " + conv, val.isEqual(conv));
        }
    }
}