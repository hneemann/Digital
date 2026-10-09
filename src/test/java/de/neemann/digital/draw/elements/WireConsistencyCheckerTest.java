/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.elements;

import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.io.Out;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.shapes.ShapeFactory;
import junit.framework.TestCase;

import java.util.ArrayList;

import static de.neemann.digital.draw.graphics.Vector.vec;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE;

/**
 */
public class WireConsistencyCheckerTest extends TestCase {

    public void testCheck() throws Exception {
        ArrayList<Wire> wires = new ArrayList<>();
        wires.add(new Wire(new Vector(0, 0), new Vector(10, 0)));
        wires.add(new Wire(new Vector(10, 0), new Vector(10, 10)));
        wires.add(new Wire(new Vector(10, 0), new Vector(20, 0)));
        ArrayList<VisualElement> visualElements = new ArrayList<>();

        wires = new WireConsistencyChecker(wires, visualElements).check();

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
        ArrayList<VisualElement> visualElements = new ArrayList<>();

        wires = new WireConsistencyChecker(wires, visualElements).check();

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
        ArrayList<VisualElement> visualElements = new ArrayList<>();

        wires = new WireConsistencyChecker(wires, visualElements).check();

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
        ArrayList<VisualElement> visualElements = new ArrayList<>();

        wires = new WireConsistencyChecker(wires, visualElements).check();

        assertEquals(2, wires.size());
        checkContains(wires, new Wire(new Vector(0, 10), new Vector(20, 10)));
        checkContains(wires, new Wire(new Vector(10, 0), new Vector(10, 20)));
    }

    public void testCheck5() throws Exception {
        ArrayList<Wire> wires = new ArrayList<>();
        wires.add(new Wire(new Vector(0, 10), new Vector(0, 20)));
        wires.add(new Wire(new Vector(0, 20), new Vector(0, 30)));
        ArrayList<VisualElement> visualElements = new ArrayList<>();
        visualElements.add(new VisualElement(new VisualElement(Out.DESCRIPTION.getName()).setPos(vec(0, 20))
                .setAttribute(Keys.LABEL, "out").setShapeFactory(new ShapeFactory(new ElementLibrary()))));

        wires = new WireConsistencyChecker(wires, visualElements).check();

        assertEquals(2, wires.size());
        checkContains(wires, new Wire(new Vector(0, 10), new Vector(0, 20)));
        checkContains(wires, new Wire(new Vector(0, 20), new Vector(0, 30)));
    }

    public void testCheck6() throws Exception {
        ArrayList<Wire> wires = new ArrayList<>();
        wires.add(new Wire(new Vector(0, 10), new Vector(0, 30)));
        ArrayList<VisualElement> visualElements = new ArrayList<>();
        visualElements.add(new VisualElement(new VisualElement(Out.DESCRIPTION.getName()).setPos(vec(0, 20))
                .setAttribute(Keys.LABEL, "out").setShapeFactory(new ShapeFactory(new ElementLibrary()))));

        wires = new WireConsistencyChecker(wires, visualElements).check();

        assertEquals(1, wires.size());
        checkContains(wires, new Wire(new Vector(0, 10), new Vector(0, 30)));
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
