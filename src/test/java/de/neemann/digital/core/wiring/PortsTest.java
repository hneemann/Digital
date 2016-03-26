package de.neemann.digital.core.wiring;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import junit.framework.TestCase;

/**
 * @author hneemann
 */
public class PortsTest extends TestCase {

    public void testPortsSimple() throws Exception {
        Splitter.Ports p = new Splitter.Ports("1,2,1,4");
        ObservableValue[] outs = p.getOutputs();
        assertEquals(4, outs.length);
        assertEquals(1, outs[0].getBits());
        assertEquals(2, outs[1].getBits());
        assertEquals(1, outs[2].getBits());
        assertEquals(4, outs[3].getBits());
    }

    public void testPortsMult() throws Exception {
        Splitter.Ports p = new Splitter.Ports("1*4,4*2");
        ObservableValue[] outs = p.getOutputs();
        assertEquals(6, outs.length);
        assertEquals(1, outs[0].getBits());
        assertEquals(1, outs[1].getBits());
        assertEquals(1, outs[2].getBits());
        assertEquals(1, outs[3].getBits());
        assertEquals(4, outs[4].getBits());
        assertEquals(4, outs[5].getBits());
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