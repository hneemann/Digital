/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.model;

import de.neemann.digital.core.element.Keys;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.elements.Wire;
import de.neemann.digital.draw.graphics.Vector;
import junit.framework.TestCase;

/**
 */
public class NetListTest extends TestCase {

    public void testSimple() throws Exception {
        Circuit c = new Circuit();

        c.add(new Wire(new Vector(1, 1), new Vector(1, 2)));
        c.add(new Wire(new Vector(2, 1), new Vector(2, 2)));
        c.add(new Wire(new Vector(1, 2), new Vector(2, 2)));
        c.add(new Wire(new Vector(1, 1), new Vector(2, 1)));

        NetList ns = new NetList(c);
        assertEquals(1, ns.size());
    }

    private void addTunnel(Circuit c, Vector pos, String name) {
        VisualElement ve = new VisualElement("Tunnel")
                .setPos(pos);
        ve.getElementAttributes().set(Keys.NETNAME, name);
        c.add(ve);
    }

    public void testTunnel() throws Exception {
        Circuit c = new Circuit();

        c.add(new Wire(new Vector(1, 1), new Vector(2, 1)));
        addTunnel(c, new Vector(2, 1), "A");

        c.add(new Wire(new Vector(3, 1), new Vector(4, 1)));
        addTunnel(c, new Vector(3, 1), "A");

        NetList ns = new NetList(c);
        assertEquals(1, ns.size());
    }


    public void testTunnel2() throws Exception {
        Circuit c = new Circuit();

        c.add(new Wire(new Vector(1, 1), new Vector(2, 1)));
        addTunnel(c, new Vector(2, 1), "A");

        c.add(new Wire(new Vector(3, 1), new Vector(4, 1)));
        addTunnel(c, new Vector(3, 1), "A");

        c.add(new Wire(new Vector(1, 4), new Vector(2, 4)));
        addTunnel(c, new Vector(2, 4), "B");

        c.add(new Wire(new Vector(3, 4), new Vector(4, 4)));
        addTunnel(c, new Vector(3, 4), "B");


        NetList ns = new NetList(c);
        assertEquals(2, ns.size());
    }

    public void testTunnel3() throws Exception {
        Circuit c = new Circuit();

        c.add(new Wire(new Vector(1, 1), new Vector(2, 1)));
        addTunnel(c, new Vector(2, 1), "A");

        c.add(new Wire(new Vector(3, 1), new Vector(4, 1)));
        addTunnel(c, new Vector(3, 1), "A");
        addTunnel(c, new Vector(4, 1), "C");

        c.add(new Wire(new Vector(1, 4), new Vector(2, 4)));
        addTunnel(c, new Vector(2, 4), "B");
        addTunnel(c, new Vector(1, 4), "C");

        c.add(new Wire(new Vector(3, 4), new Vector(4, 4)));
        addTunnel(c, new Vector(3, 4), "B");


        NetList ns = new NetList(c);
        assertEquals(1, ns.size());
    }

}

