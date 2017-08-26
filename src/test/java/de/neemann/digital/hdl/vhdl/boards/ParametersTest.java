package de.neemann.digital.hdl.vhdl.boards;

import junit.framework.TestCase;

public class ParametersTest extends TestCase {

    public void testParameters() {
        ClockIntegratorARTIX7.Params p;
        p = new ClockIntegratorARTIX7.Parameters(10000000, 10).getBest();
        assertEquals(1, p.getD());
        assertEquals(12, p.getM());
        assertEquals(120, p.getDivider());
        p = new ClockIntegratorARTIX7.Parameters(5000000, 10).getBest();
        assertEquals(2, p.getD());
        assertEquals(12, p.getM());
        assertEquals(120, p.getDivider());
        p = new ClockIntegratorARTIX7.Parameters(20000000, 10).getBest();
        assertEquals(1, p.getD());
        assertEquals(12, p.getM());
        assertEquals(60, p.getDivider());
    }

    public void testInvalid() {
        assertNull(new ClockIntegratorARTIX7.Parameters(1000000, 10).getBest());
        assertNull(new ClockIntegratorARTIX7.Parameters(4000000, 10).getBest());
    }

}