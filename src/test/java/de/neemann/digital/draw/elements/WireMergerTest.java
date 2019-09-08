/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.elements;

import de.neemann.digital.draw.graphics.Vector;
import junit.framework.TestCase;

import java.util.ArrayList;

/**
 */
public class WireMergerTest extends TestCase {

    public void testHorizontal() {
        WireMerger.OrientationHandler handler = new WireMerger.OrientationHandlerHorizontal();

        assertEquals(1, handler.getS(new Vector(1, 3)));
        assertEquals(3, handler.getWireClass(new Vector(1, 3)));

        Wire wire = handler.toWire(new WireMerger.SimpleWire(1, 3, 7));
        assertEquals(new Vector(3, 1), wire.p1);
        assertEquals(new Vector(7, 1), wire.p2);
    }

    public void testVertical() {
        WireMerger.OrientationHandler handler = new WireMerger.OrientationHandlerVertical();

        assertEquals(1, handler.getS(new Vector(3, 1)));
        assertEquals(3, handler.getWireClass(new Vector(3, 1)));


        Wire wire = handler.toWire(new WireMerger.SimpleWire(1, 3, 7));
        assertEquals(new Vector(1, 3), wire.p1);
        assertEquals(new Vector(1, 7), wire.p2);
    }

    public void testMerge1() {
        WireMerger wm = new WireMerger(Wire.Orientation.horizontal);
        wm.add(new Wire(new Vector(1, 3), new Vector(5, 3)));
        wm.add(new Wire(new Vector(5, 3), new Vector(8, 3)));
        wm.add(new Wire(new Vector(1, 4), new Vector(5, 4)));
        wm.add(new Wire(new Vector(5, 5), new Vector(8, 5)));

        wm.add(new Wire(new Vector(1, 6), new Vector(6, 6)));
        wm.add(new Wire(new Vector(4, 6), new Vector(8, 6)));

        wm.add(new Wire(new Vector(1, 7), new Vector(4, 7)));
        wm.add(new Wire(new Vector(5, 7), new Vector(8, 7)));


        ArrayList<Wire> newWires = new ArrayList<>();
        wm.addTo(newWires);

        assertEquals(6, newWires.size());
        assertTrue(new Wire(new Vector(1, 3), new Vector(8, 3)).isIncludedIn(newWires));
        assertTrue(new Wire(new Vector(1, 4), new Vector(5, 4)).isIncludedIn(newWires));
        assertTrue(new Wire(new Vector(5, 5), new Vector(8, 5)).isIncludedIn(newWires));

        assertTrue(new Wire(new Vector(1, 6), new Vector(8, 6)).isIncludedIn(newWires));

        assertTrue(new Wire(new Vector(1, 7), new Vector(4, 7)).isIncludedIn(newWires));
        assertTrue(new Wire(new Vector(5, 7), new Vector(8, 7)).isIncludedIn(newWires));
    }

    public void testMerge2() {
        WireMerger wm = new WireMerger(Wire.Orientation.horizontal);
        wm.add(new Wire(new Vector(1, 3), new Vector(3, 3)));
        wm.add(new Wire(new Vector(6, 3), new Vector(8, 3)));
        wm.add(new Wire(new Vector(2, 3), new Vector(7, 3)));

        ArrayList<Wire> newWires = new ArrayList<>();
        wm.addTo(newWires);
        assertEquals(1, newWires.size());
        assertTrue(new Wire(new Vector(1, 3), new Vector(8, 3)).equalsContent(newWires.get(0)));
    }
}
