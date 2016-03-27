package de.neemann.digital.draw.model;

import de.neemann.digital.draw.elements.Wire;
import de.neemann.digital.draw.graphics.Vector;
import junit.framework.TestCase;

import java.util.ArrayList;

/**
 * @author hneemann
 */
public class NetListTest extends TestCase {

    public void testSimple() throws Exception {
        ArrayList<Wire> w = new ArrayList<>();

        w.add(new Wire(new Vector(1, 1), new Vector(1, 2)));
        w.add(new Wire(new Vector(2, 1), new Vector(2, 2)));
        w.add(new Wire(new Vector(1, 2), new Vector(2, 2)));
        w.add(new Wire(new Vector(1, 1), new Vector(2, 1)));

        NetList ns = new NetList(w);
        assertEquals(1, ns.size());

    }
}