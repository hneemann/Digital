package de.neemann.digital.builder;

import de.neemann.digital.builder.jedec.FuseMapFillerException;
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

    public void testDoubleAssignment() throws FuseMapFillerException {
        pinMap.assignPin("a", 2);
        try {
            pinMap.assignPin("b", 2);
            assertTrue(false);
        } catch (FuseMapFillerException e) {
            assertTrue(true);
        }
    }

    public void testDoubleAssignment2() throws FuseMapFillerException {
        pinMap.assignPin("a", 2);
        try {
            pinMap.assignPin("a", 3);
            assertTrue(false);
        } catch (FuseMapFillerException e) {
            assertTrue(true);
        }
    }

    public void testInputs() throws FuseMapFillerException {
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
        } catch (FuseMapFillerException e) {
            assertTrue(true);
        }
    }

    public void testOutputs() throws FuseMapFillerException {
        pinMap.assignPin("a", 2);
        assertEquals(2, pinMap.getOutputFor("a"));
        assertEquals(2, pinMap.getOutputFor("a"));
        assertEquals(1, pinMap.getOutputFor("b"));
        assertEquals(1, pinMap.getOutputFor("b"));

        try {
            pinMap.getOutputFor("c");
            assertTrue(false);
        } catch (FuseMapFillerException e) {
            assertTrue(true);
        }
    }
}