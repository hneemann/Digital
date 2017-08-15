package de.neemann.digital.hdl.model;

import junit.framework.TestCase;

public class HDLConstantTest extends TestCase {

    public void testSimple() throws HDLException {
        assertEquals("'0'", new HDLConstant(0, 1).vhdlValue());
        assertEquals("'1'", new HDLConstant(1, 1).vhdlValue());
        assertEquals("\"00\"", new HDLConstant(0, 2).vhdlValue());
        assertEquals("\"10\"", new HDLConstant(2, 2).vhdlValue());

        assertEquals("'Z'", new HDLConstant(HDLConstant.Type.highz, 1).vhdlValue());
        assertEquals("\"ZZ\"", new HDLConstant(HDLConstant.Type.highz, 2).vhdlValue());

        assertEquals("'H'", new HDLConstant(HDLConstant.Type.weakHigh, 1).vhdlValue());
        assertEquals("\"HH\"", new HDLConstant(HDLConstant.Type.weakHigh, 2).vhdlValue());

        assertEquals("'-'", new HDLConstant(HDLConstant.Type.dontcare, 1).vhdlValue());
        assertEquals("\"--\"", new HDLConstant(HDLConstant.Type.dontcare, 2).vhdlValue());
    }
}