package de.neemann.digital.hdl.hgs;

import junit.framework.TestCase;

public class ValueTest extends TestCase {

    public void testTrimRight() {
        assertEquals("", Value.trimRight(""));
        assertEquals("", Value.trimRight("\n"));
        assertEquals("a", Value.trimRight("a"));
        assertEquals("a", Value.trimRight("a\n \t"));
        assertEquals("a", Value.trimRight("a  \n"));
    }

    public void testTrimLeft() {
        assertEquals("", Value.trimLeft(""));
        assertEquals("", Value.trimLeft("\n"));
        assertEquals("a", Value.trimLeft("a"));
        assertEquals("a", Value.trimLeft("\n \ta"));
        assertEquals("a", Value.trimLeft("  \na"));
    }
}