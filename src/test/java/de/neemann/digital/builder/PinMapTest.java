/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.builder;

import de.neemann.digital.analyse.expression.Variable;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

/**
 *
 */
public class PinMapTest extends TestCase {

    private PinMap pinMap;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        pinMap = new PinMap()
                .setAvailInputs(1, 2, 3)
                .setAvailOutputs(4, 5, 6);

    }

    public void testDoubleAssignment() throws PinMapException {
        pinMap.assignPin("a", 2);
        try {
            pinMap.assignPin("b", 2);
            fail();
        } catch (PinMapException e) {
            assertTrue(true);
        }
    }

    public void testDoubleAssignment2() throws PinMapException {
        pinMap.assignPin("a", 2);
        try {
            pinMap.assignPin("a", 3);
            fail();
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
            fail();
        } catch (PinMapException e) {
            assertTrue(true);
        }
    }

    public void testOutputs() throws PinMapException {
        pinMap.assignPin("a", 5);
        assertEquals(5, pinMap.getOutputFor("a"));
        assertEquals(5, pinMap.getOutputFor("a"));
        assertEquals(4, pinMap.getOutputFor("b"));
        assertEquals(4, pinMap.getOutputFor("b"));
        assertEquals(6, pinMap.getOutputFor("d"));

        try {
            pinMap.getOutputFor("c");
            fail();
        } catch (PinMapException e) {
            assertTrue(true);
        }
    }

    public void testBidirectional() throws PinMapException {
        pinMap = new PinMap()
                .setAvailBidirectional(1, 2, 5);
        pinMap.assignPin("a", 5);
        assertEquals(5, pinMap.getOutputFor("a"));
        assertEquals(5, pinMap.getOutputFor("a"));
        assertEquals(1, pinMap.getInputFor("b"));
        assertEquals(1, pinMap.getInputFor("b"));
        assertEquals(2, pinMap.getOutputFor("d"));

        try {
            pinMap.getOutputFor("c");
            fail();
        } catch (PinMapException e) {
            assertTrue(true);
        }
    }

    public void testParse() throws PinMapException {
        pinMap.parseString("a=5, Q_0=6");
        assertEquals(6, pinMap.getOutputFor("Q_0"));
        assertEquals(5, pinMap.getOutputFor("a"));
    }

    public void testParse2() throws PinMapException {
        pinMap.parseString("a=5").parseString("Q_0=6");
        assertEquals(6, pinMap.getOutputFor("Q_0"));
        assertEquals(5, pinMap.getOutputFor("a"));
    }

    public void testParse3() {
        try {
            pinMap.parseString("a0");
            fail();
        } catch (PinMapException e) {
            assertTrue(true);
        }

        try {
            pinMap.parseString("a=");
            fail();
        } catch (PinMapException e) {
            assertTrue(true);
        }

        try {
            pinMap.parseString("=7");
            fail();
        } catch (PinMapException e) {
            assertTrue(true);
        }
    }

    public void testAlias() throws PinMapException {
        pinMap.assignPin("A", 4);
        assertTrue(pinMap.isSimpleAlias("B", new Variable("A"), null));

        assertEquals(4, pinMap.getOutputFor("A"));
        assertEquals(4, pinMap.getOutputFor("B"));
    }

    public void testAliasSwap() throws PinMapException {
        pinMap.assignPin("A", 4);
        assertTrue(pinMap.isSimpleAlias("A", new Variable("B"), null));

        assertEquals(4, pinMap.getOutputFor("A"));
        assertEquals(4, pinMap.getOutputFor("B"));
    }

    public void testAliasReverseOrder() throws PinMapException {
        assertTrue(pinMap.isSimpleAlias("B", new Variable("A"), null));
        pinMap.assignPin("A", 4);

        assertEquals(4, pinMap.getOutputFor("A"));
        assertEquals(4, pinMap.getOutputFor("B"));
    }


    public void testAliasInput() throws PinMapException {
        pinMap.assignPin("A", 2);
        assertTrue(pinMap.isSimpleAlias("B", new Variable("A"), null));

        assertEquals(2, pinMap.getInputFor("A"));
        assertEquals(2, pinMap.getInputFor("B"));
    }

    public void testAliasSequential() throws PinMapException {
        pinMap.assignPin("A", 4);
        assertFalse(pinMap.isSimpleAlias("B", new Variable("A"), new HashSet<>()));

        assertEquals(4, pinMap.getOutputFor("A"));
        assertEquals(5, pinMap.getOutputFor("B"));
    }

    public void testAliasSequential2() throws PinMapException {
        pinMap.assignPin("A", 4);
        assertTrue(pinMap.isSimpleAlias("B", new Variable("A"), new HashSet<>(Collections.singletonList("A"))));

        assertEquals(4, pinMap.getOutputFor("A"));
        assertEquals(4, pinMap.getOutputFor("B"));
    }

    public void testToString() throws PinMapException {
        pinMap.assignPin("A", 1);
        pinMap.assignPin("B", 4);
        String pinStr = pinMap.toString();

        assertTrue(pinStr.contains("1: A\n"));
        assertTrue(pinStr.contains("4: B\n"));
    }

}
