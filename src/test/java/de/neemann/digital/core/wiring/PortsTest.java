package de.neemann.digital.core.wiring;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValues;
import junit.framework.TestCase;

/**
 * @author hneemann
 */
public class PortsTest extends TestCase {

    public void testPortsSimple() throws Exception {
        Splitter.Ports p = new Splitter.Ports("1,2,1,4");
        ObservableValues outs = p.getOutputs(false);
        assertEquals(4, outs.size());
        assertEquals(1, outs.get(0).getBits());
        assertEquals(2, outs.get(1).getBits());
        assertEquals(1, outs.get(2).getBits());
        assertEquals(4, outs.get(3).getBits());
    }

    public void testPortsMult() throws Exception {
        Splitter.Ports p = new Splitter.Ports("1*4,4*2");
        ObservableValues outs = p.getOutputs(false);
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
}