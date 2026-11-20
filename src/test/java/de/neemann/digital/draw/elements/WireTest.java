/*
 * Copyright (c) 2019 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.elements;

import de.neemann.digital.core.Value;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.Vector;
import junit.framework.TestCase;

import java.awt.Color;

public class WireTest extends TestCase {

    public void testCopyKeepsColor() {
        Color color = new Color(12, 34, 56);
        Wire wire = new Wire(new Vector(5, 5), new Vector(10, 5)).setColor(color);

        assertEquals(color, new Wire(wire).getColor());
    }

    public void testColoredWireUsesSignalColorAroundCore() {
        Color color = new Color(40, 80, 120);
        Value low = new Value(0, 1);
        Value high = new Value(1, 1);

        assertEquals(Style.getWireStyle(low).getColor(), Wire.getWireStyle(color, low).getColor());
        assertEquals(color, Wire.getWireCoreStyle(color, low).getColor());
        assertEquals(Style.getWireStyle(high).getColor(), Wire.getWireStyle(color, high).getColor());
        assertEquals(color, Wire.getWireCoreStyle(color, high).getColor());
        assertTrue(Wire.getWireStyle(color, high).getThickness() > Wire.getWireCoreStyle(color, high).getThickness());
        assertEquals(Style.getWireStyle(new Value(1)).getColor(), Wire.getWireStyle(color, new Value(1)).getColor());
    }

    public void testDefaultWireKeepsSignalColorAndDefaultCore() {
        Value low = new Value(0, 1);
        Value high = new Value(1, 1);

        assertEquals(Style.getWireStyle(low).getColor(), Wire.getWireStyle(null, low).getColor());
        assertEquals(Style.getWireStyle(high).getColor(), Wire.getWireStyle(null, high).getColor());
        assertEquals(Style.WIRE.getColor(), Wire.getWireCoreStyle(null, low).getColor());
        assertEquals(Style.WIRE.getColor(), Wire.getWireCoreStyle(null, high).getColor());
        assertNull(Wire.getWireCoreStyle(null, null));
    }

    public void testDistance() {
        Wire w = new Wire(new Vector(5,5), new Vector(10,5));
        assertEquals(5, w.distance(new Vector(0,5)),1e-4f);
        assertEquals(5, w.distance(new Vector(15,5)),1e-4f);
        assertEquals(5, w.distance(new Vector(5,0)),1e-4f);
        assertEquals(5, w.distance(new Vector(5,10)),1e-4f);
        assertEquals(5, w.distance(new Vector(10,0)),1e-4f);
        assertEquals(5, w.distance(new Vector(10,10)),1e-4f);
        assertEquals(5, w.distance(new Vector(7,0)),1e-4f);
        assertEquals(5, w.distance(new Vector(7,10)),1e-4f);
        assertEquals(6, w.distance(new Vector(8,-1)),1e-4f);
        assertEquals(6, w.distance(new Vector(8,11)),1e-4f);
    }
}