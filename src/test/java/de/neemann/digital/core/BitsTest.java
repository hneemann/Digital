package de.neemann.digital.core;

import junit.framework.TestCase;

public class BitsTest extends TestCase {

    public void testMask() {
        assertEquals(1, Bits.mask(1));
        assertEquals(0x7fffffffffffffffL, Bits.mask(63));
        assertEquals(-1, Bits.mask(64));
        assertEquals(-1, Bits.mask(65));
    }

    public void testDown() {
        assertEquals(1, Bits.down(2, 1));
        assertEquals(0x7fffffffffffffffL, Bits.down(-1, 1));
        assertEquals(0L, Bits.down(0xff, 64));
    }

    public void testUp() throws BitsException {
        assertEquals(0x8000000000000000L, Bits.up(1, 63));
        assertEquals(2L, Bits.up(1, 1));
        assertEquals(4L, Bits.up(1, 2));
        assertEquals(0x8000000000000000L, Bits.up(0xff, 63));
        assertEquals(0L, Bits.up(1, 64));
    }

    public void testSignedFlag() {
        assertEquals(1, Bits.signedFlagMask(1));
        assertEquals(0x8000000000000000L, Bits.signedFlagMask(64));
    }

    public void testIsNegative() {
        assertTrue(Bits.isNegative(7, 3));
        assertFalse(Bits.isNegative(3, 3));
        assertTrue(Bits.isNegative(0x8000000000000000L, 64));
        assertFalse(Bits.isNegative(0x4000000000000000L, 64));
    }

    public void testBitsLn2() {
        assertEquals(1, Bits.binLn2(0));
        assertEquals(1, Bits.binLn2(1));
        assertEquals(2, Bits.binLn2(2));
        assertEquals(2, Bits.binLn2(3));
        assertEquals(3, Bits.binLn2(4));
        assertEquals(3, Bits.binLn2(5));
        assertEquals(3, Bits.binLn2(6));
        assertEquals(3, Bits.binLn2(7));
        assertEquals(4, Bits.binLn2(8));
        assertEquals(4, Bits.binLn2(15));
        assertEquals(5, Bits.binLn2(16));
        assertEquals(5, Bits.binLn2(31));
        assertEquals(6, Bits.binLn2(32));
    }
}