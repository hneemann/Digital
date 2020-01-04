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
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.shapes.ShapeFactory;
import junit.framework.TestCase;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;

/**
 *
 */
public class NetListTest extends TestCase {

    public void testSimple() throws Exception {
        Circuit c = new Circuit();

        c.add(new Wire(new Vector(1 * SIZE, 1 * SIZE), new Vector(1 * SIZE, 2 * SIZE)));
        c.add(new Wire(new Vector(2 * SIZE, 1 * SIZE), new Vector(2 * SIZE, 2 * SIZE)));
        c.add(new Wire(new Vector(1 * SIZE, 2 * SIZE), new Vector(2 * SIZE, 2 * SIZE)));
        c.add(new Wire(new Vector(1 * SIZE, 1 * SIZE), new Vector(2 * SIZE, 1 * SIZE)));

        NetList ns = new NetList(c);
        assertEquals(1, ns.size());
    }

    private void addTunnel(Circuit c, Vector pos, String name, ShapeFactory sf) {
        VisualElement ve = new VisualElement("Tunnel")
                .setPos(pos).setShapeFactory(sf);
        ve.getElementAttributes().set(Keys.NETNAME, name);
        c.add(ve);
    }

    public void testTunnel() throws Exception {
        ElementLibrary lib = new ElementLibrary();
        ShapeFactory sf = new ShapeFactory(lib);
        Circuit c = new Circuit();

        c.add(new Wire(new Vector(1 * SIZE, 1 * SIZE), new Vector(2 * SIZE, 1 * SIZE)));
        addTunnel(c, new Vector(2 * SIZE, 1 * SIZE), "A", sf);

        c.add(new Wire(new Vector(3 * SIZE, 1 * SIZE), new Vector(4 * SIZE, 1 * SIZE)));
        addTunnel(c, new Vector(3 * SIZE, 1 * SIZE), "A", sf);

        NetList ns = new NetList(c);
        assertEquals(1, ns.size());
    }


    public void testTunnel2() throws Exception {
        ElementLibrary lib = new ElementLibrary();
        ShapeFactory sf = new ShapeFactory(lib);
        Circuit c = new Circuit();

        c.add(new Wire(new Vector(1 * SIZE, 1 * SIZE), new Vector(2 * SIZE, 1 * SIZE)));
        addTunnel(c, new Vector(2 * SIZE, 1 * SIZE), "A", sf);

        c.add(new Wire(new Vector(3 * SIZE, 1 * SIZE), new Vector(4 * SIZE, 1 * SIZE)));
        addTunnel(c, new Vector(3 * SIZE, 1 * SIZE), "A", sf);

        c.add(new Wire(new Vector(1 * SIZE, 4 * SIZE), new Vector(2 * SIZE, 4 * SIZE)));
        addTunnel(c, new Vector(2 * SIZE, 4 * SIZE), "B", sf);

        c.add(new Wire(new Vector(3 * SIZE, 4 * SIZE), new Vector(4 * SIZE, 4 * SIZE)));
        addTunnel(c, new Vector(3 * SIZE, 4 * SIZE), "B", sf);


        NetList ns = new NetList(c);
        assertEquals(2, ns.size());
    }

    public void testTunnel3() throws Exception {
        ElementLibrary lib = new ElementLibrary();
        ShapeFactory sf = new ShapeFactory(lib);
        Circuit c = new Circuit();

        c.add(new Wire(new Vector(1 * SIZE, 1 * SIZE), new Vector(2 * SIZE, 1 * SIZE)));
        addTunnel(c, new Vector(2 * SIZE, 1 * SIZE), "A", sf);

        c.add(new Wire(new Vector(3 * SIZE, 1 * SIZE), new Vector(4 * SIZE, 1 * SIZE)));
        addTunnel(c, new Vector(3 * SIZE, 1 * SIZE), "A", sf);
        addTunnel(c, new Vector(4 * SIZE, 1 * SIZE), "C", sf);

        c.add(new Wire(new Vector(1 * SIZE, 4 * SIZE), new Vector(2 * SIZE, 4 * SIZE)));
        addTunnel(c, new Vector(2 * SIZE, 4 * SIZE), "B", sf);
        addTunnel(c, new Vector(1 * SIZE, 4 * SIZE), "C", sf);

        c.add(new Wire(new Vector(3 * SIZE, 4 * SIZE), new Vector(4 * SIZE, 4 * SIZE)));
        addTunnel(c, new Vector(3 * SIZE, 4 * SIZE), "B", sf);


        NetList ns = new NetList(c);
        assertEquals(1, ns.size());
    }

}

