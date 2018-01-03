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
}