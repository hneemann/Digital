package de.neemann.digital.core.io;

import de.neemann.digital.core.Value;
import junit.framework.TestCase;

public class IntFormatTest extends TestCase {

    public void testHex() throws Exception {
        assertEquals("1", IntFormat.hex.format(new Value(1, 1)));
        assertEquals("1", IntFormat.hex.format(new Value(1, 2)));
        assertEquals("1", IntFormat.hex.format(new Value(1, 3)));
        assertEquals("1", IntFormat.hex.format(new Value(1, 4)));
        assertEquals("F", IntFormat.hex.format(new Value(-1, 4)));
        assertEquals("01", IntFormat.hex.format(new Value(1, 5)));
        assertEquals("1F", IntFormat.hex.format(new Value(-1, 5)));
        assertEquals("FFF", IntFormat.hex.format(new Value(-1, 12)));
        assertEquals("1FFF", IntFormat.hex.format(new Value(-1, 13)));
        assertEquals("3FFF", IntFormat.hex.format(new Value(-1, 14)));
        assertEquals("7FFF", IntFormat.hex.format(new Value(-1, 15)));
        assertEquals("FFFF", IntFormat.hex.format(new Value(-1, 16)));
        assertEquals("FEDCBA9876543210", IntFormat.hex.format(new Value(0xFEDCBA9876543210L, 64)));
    }

    public void testBin() throws Exception {
        assertEquals("1", IntFormat.bin.format(new Value(1, 1)));
        assertEquals("01", IntFormat.bin.format(new Value(1, 2)));
        assertEquals("001", IntFormat.bin.format(new Value(1, 3)));
        assertEquals("111", IntFormat.bin.format(new Value(-1, 3)));
    }

    public void testDec() throws Exception {
        assertEquals("3", IntFormat.dec.format(new Value(-1, 2)));
        assertEquals("-1", IntFormat.decSigned.format(new Value(-1, 2)));
    }
}