/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.wiring;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValues;
import junit.framework.TestCase;

/**
 */
public class PortsTest extends TestCase {

    public void testPortsSimple() throws Exception {
        Splitter.Ports p = new Splitter.Ports("1,2,1,4");
        ObservableValues outs = p.getOutputs();
        assertEquals(4, outs.size());
        assertEquals(1, outs.get(0).getBits());
        assertEquals(2, outs.get(1).getBits());
        assertEquals(1, outs.get(2).getBits());
        assertEquals(4, outs.get(3).getBits());
    }

    public void testPortsMult() throws Exception {
        Splitter.Ports p = new Splitter.Ports("1*4,4*2");
        ObservableValues outs = p.getOutputs();
        assertEquals(6, outs.size());
        assertEquals(1, outs.get(0).getBits());
        assertEquals(1, outs.get(1).getBits());
        assertEquals(1, outs.get(2).getBits());
        assertEquals(1, outs.get(3).getBits());
        assertEquals(4, outs.get(4).getBits());
        assertEquals(4, outs.get(5).getBits());
    }

    public void testPortsError() throws Exception {
        try {
            new Splitter.Ports("1*4,4*");
            assertTrue(false);
        } catch (NodeException e) {
            assertTrue(true);
        }
    }

    public void testPortsRange() throws Exception {
        Splitter.Ports p = new Splitter.Ports("4-6,0-3");
        ObservableValues outs = p.getOutputs();
        assertEquals(2, outs.size());
        assertEquals(3, outs.get(0).getBits());
        assertEquals(4, outs.get(1).getBits());
    }

    public void testPortsRangeOneBit() throws Exception {
        Splitter.Ports p = new Splitter.Ports("4-4,3-3,2-2,1-1");
        ObservableValues outs = p.getOutputs();
        assertEquals(4, outs.size());
        assertEquals(1, outs.get(0).getBits());
        assertEquals(1, outs.get(1).getBits());
        assertEquals(1, outs.get(2).getBits());
        assertEquals(1, outs.get(3).getBits());
    }

    public void testInputConsistency() throws Exception {
        Splitter.Ports p = new Splitter.Ports("4-7,0-2");
        try {
            p.checkInputConsistency();
            fail();
        } catch (NodeException e) {
            assertTrue(true);
        }
    }

    public void testInputConsistency2() throws Exception {
        Splitter.Ports p = new Splitter.Ports("4-7,0-4");
        try {
            p.checkInputConsistency();
            fail();
        } catch (NodeException e) {
            assertTrue(true);
        }
    }

    public void testInputConsistency4() throws Exception {
        Splitter.Ports p = new Splitter.Ports("1*64");
        try {
            p = new Splitter.Ports("1*65");
            fail();
        } catch (NodeException e) {
            assertTrue(true);
        }
    }

    public void testInputConsistency3() throws Exception {
        Splitter.Ports p = new Splitter.Ports("7-4,4-4");
        assertEquals(4, p.getPort(0).getPos());
        assertEquals(4, p.getPort(0).getBits());
        assertEquals(4, p.getPort(1).getPos());
        assertEquals(1, p.getPort(1).getBits());
    }


}
