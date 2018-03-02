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
public class WireConsistencyCheckerTest extends TestCase {

    public void testCheck() throws Exception {
        ArrayList<Wire> wires = new ArrayList<>();
        wires.add(new Wire(new Vector(0, 0), new Vector(10, 0)));
        wires.add(new Wire(new Vector(10, 0), new Vector(10, 10)));
        wires.add(new Wire(new Vector(10, 0), new Vector(20, 0)));

        wires = new WireConsistencyChecker(wires).check();

        assertEquals(3, wires.size());
        checkContains(wires, new Wire(new Vector(0, 0), new Vector(10, 0)));
        checkContains(wires, new Wire(new Vector(10, 0), new Vector(20, 0)));
        checkContains(wires, new Wire(new Vector(10, 0), new Vector(10, 10)));
    }

    public void testCheck2() throws Exception {
        ArrayList<Wire> wires = new ArrayList<>();
        wires.add(new Wire(new Vector(0, 0), new Vector(10, 0)));
        wires.add(new Wire(new Vector(10, 0), new Vector(10, 10)));
        wires.add(new Wire(new Vector(0, 0), new Vector(20, 0)));

        wires = new WireConsistencyChecker(wires).check();

        assertEquals(3, wires.size());
        checkContains(wires, new Wire(new Vector(0, 0), new Vector(10, 0)));
        checkContains(wires, new Wire(new Vector(10, 0), new Vector(20, 0)));
        checkContains(wires, new Wire(new Vector(10, 0), new Vector(10, 10)));
    }

    public void testCheck3() throws Exception {
        ArrayList<Wire> wires = new ArrayList<>();
        wires.add(new Wire(new Vector(0, 0), new Vector(10, 0)));
        wires.add(new Wire(new Vector(10, 0), new Vector(20, 0)));
        wires.add(new Wire(new Vector(10, 0), new Vector(10, 10)));
        wires.add(new Wire(new Vector(10, 0), new Vector(10, -10)));

        wires = new WireConsistencyChecker(wires).check();

        assertEquals(4, wires.size());
        checkContains(wires, new Wire(new Vector(0, 0), new Vector(10, 0)));
        checkContains(wires, new Wire(new Vector(10, 0), new Vector(20, 0)));
        checkContains(wires, new Wire(new Vector(10, 0), new Vector(10, 10)));
        checkContains(wires, new Wire(new Vector(10, 0), new Vector(10, -10)));
    }

    public void testCheck4() throws Exception {
        ArrayList<Wire> wires = new ArrayList<>();
        wires.add(new Wire(new Vector(0, 10), new Vector(20, 10)));
        wires.add(new Wire(new Vector(10, 0), new Vector(10, 20)));

        wires = new WireConsistencyChecker(wires).check();

        assertEquals(2, wires.size());
        checkContains(wires, new Wire(new Vector(0, 10), new Vector(20, 10)));
        checkContains(wires, new Wire(new Vector(10, 0), new Vector(10, 20)));
    }

    public static void checkContains(ArrayList<Wire> wires, Wire wire) {
        for (Wire w : wires)
            if (wire.equalsContent(wire))
                return;

        wire = new Wire(wire.p2, wire.p1);

        for (Wire w : wires)
            if (w.equalsContent(wire))
                return;

        assertTrue(false);
    }
}
