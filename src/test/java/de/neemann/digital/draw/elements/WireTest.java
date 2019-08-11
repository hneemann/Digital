/*
 * Copyright (c) 2019 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.elements;

import de.neemann.digital.draw.graphics.Vector;
import junit.framework.TestCase;

public class WireTest extends TestCase {

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