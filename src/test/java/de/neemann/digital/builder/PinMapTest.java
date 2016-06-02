package de.neemann.digital.builder;

import junit.framework.TestCase;

/**
 * @author hneemann
 */
public class PinMapTest extends TestCase {

    private PinMap pinMap;

    @Override
    public void setUp() throws Exception {
        pinMap = new PinMap()
                .setAvailInputs(1, 2, 3)
                .setAvailOutputs(1, 2);

    }

    public void testDoubleAssignment() throws PinMapException {
        pinMap.assignPin("a", 2);
        try {
            pinMap.assignPin("b", 2);
            assertTrue(false);
        } catch (PinMapException e) {
            assertTrue(true);
        }
    }

    public void testDoubleAssignment2() throws PinMapException {
        pinMap.assignPin("a", 2);
        try {
            pinMap.assignPin("a", 3);
            assertTrue(false);
        } catch (PinMapException e) {
            assertTrue(true);
        }
    }

    public void testInputs() throws PinMapException {
        pinMap.assignPin("a", 2);
        assertEquals(2, pinMap.getInputFor("a"));
        assertEquals(2, pinMap.getInputFor("a"));
        assertEquals(1, pinMap.getInputFor("b"));
        assertEquals(1, pinMap.getInputFor("b"));
        assertEquals(3, pinMap.getInputFor("c"));
        assertEquals(3, pinMap.getInputFor("c"));

        try {
            pinMap.getInputFor("d");
            assertTrue(false);
        } catch (PinMapException e) {
            assertTrue(true);
        }
    }

    public void testOutputs() throws PinMapException {
        pinMap.assignPin("a", 2);
        assertEquals(2, pinMap.getOutputFor("a"));
        assertEquals(2, pinMap.getOutputFor("a"));
        assertEquals(1, pinMap.getOutputFor("b"));
        assertEquals(1, pinMap.getOutputFor("b"));

        try {
            pinMap.getOutputFor("c");
            assertTrue(false);
        } catch (PinMapException e) {
            assertTrue(true);
        }
    }
}