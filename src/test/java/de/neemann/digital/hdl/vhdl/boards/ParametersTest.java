package de.neemann.digital.hdl.vhdl.boards;

import junit.framework.TestCase;

public class ParametersTest extends TestCase {

    public void testParameters() {
        ClockIntegratorARTIX7.Params p;
        p = new ClockIntegratorARTIX7.Parameters(10000000, 10).getBest();
        assertEquals(1, p.getD());
        assertEquals(12, p.getM());
        assertEquals(120, p.getDivider());
        assertFalse(p.isCascading());
        p = new ClockIntegratorARTIX7.Parameters(5000000, 10).getBest();
        assertEquals(1, p.getD());
        assertEquals(12, p.getM());
        assertEquals(2, p.getDivider());
        assertEquals(120, p.getDivider4());
        assertTrue(p.isCascading());
        p = new ClockIntegratorARTIX7.Parameters(4700000, 10).getBest();
        assertEquals(1, p.getD());
        assertEquals(12, p.getM());
        assertEquals(3, p.getDivider());
        assertEquals(85, p.getDivider4());
        assertTrue(p.isCascading());
        p = new ClockIntegratorARTIX7.Parameters(20000000, 10).getBest();
        assertEquals(1, p.getD());
        assertEquals(12, p.getM());
        assertEquals(60, p.getDivider());
        assertFalse(p.isCascading());
    }

    public void testCascading() {
        ClockIntegratorARTIX7.Params p;
        p = new ClockIntegratorARTIX7.Parameters(1000000, 10).getBest();
        assertEquals(1, p.getD());
        assertEquals(12, p.getM());
        assertEquals(10, p.getDivider());
        assertEquals(120, p.getDivider4());
        assertTrue(p.isCascading());
        p = new ClockIntegratorARTIX7.Parameters(800000, 10).getBest();
        assertEquals(1, p.getD());
        assertEquals(12, p.getM());
        assertEquals(12, p.getDivider());
        assertEquals(125, p.getDivider4());
        assertTrue(p.isCascading());
        p = new ClockIntegratorARTIX7.Parameters(80000, 10).getBest();
        assertEquals(1, p.getD());
        assertEquals(12, p.getM());
        assertEquals(120, p.getDivider());
        assertEquals(125, p.getDivider4());
        assertTrue(p.isCascading());
        p = new ClockIntegratorARTIX7.Parameters(78000, 10).getBest();
        assertEquals(1, p.getD());
        assertEquals(12, p.getM());
        assertEquals(124, p.getDivider());
        assertEquals(124, p.getDivider4());
        assertTrue(p.isCascading());
        p = new ClockIntegratorARTIX7.Parameters(37000, 10).getBest();
        assertEquals(2, p.getD());
        assertEquals(12, p.getM());
        assertEquals(127, p.getDivider());
        assertEquals(128, p.getDivider4());
        assertTrue(p.isCascading());
    }

    public void testInvalid() {
        assertNull(new ClockIntegratorARTIX7.Parameters(36000, 10).getBest());
    }
}