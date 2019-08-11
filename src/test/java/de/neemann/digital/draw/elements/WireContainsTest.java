/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.elements;

import de.neemann.digital.draw.graphics.Vector;
import junit.framework.TestCase;

/**
 */
public class WireContainsTest extends TestCase {

    public void testHorizontal() {
        Wire w = new Wire(new Vector(0, 0), new Vector(10, 0));
        assertFalse(w.contains(new Vector(-5, 0), 5));
        assertFalse(w.contains(new Vector(15, 0), 5));
        assertFalse(w.contains(new Vector(5, 5), 5));
        assertFalse(w.contains(new Vector(5, -5), 5));

        assertTrue(w.contains(new Vector(5, 2), 5));
        assertTrue(w.contains(new Vector(5, -2), 5));

        assertTrue(w.contains(new Vector(-2, 0), 5));
        assertTrue(w.contains(new Vector(12, 0), 5));
    }

    public void testVertical() {
        Wire w = new Wire(new Vector(0, 0), new Vector(0, 10));
        assertFalse(w.contains(new Vector(0, -5), 5));
        assertFalse(w.contains(new Vector(0, 15), 5));
        assertFalse(w.contains(new Vector(5, 5), 5));
        assertFalse(w.contains(new Vector(-5, 5), 5));

        assertTrue(w.contains(new Vector(2, 5), 5));
        assertTrue(w.contains(new Vector(-2, 5), 5));

        assertTrue(w.contains(new Vector(0, -2), 5));
        assertTrue(w.contains(new Vector(0, 12), 5));
    }

    public void testDiagonal() {
        Wire w = new Wire(new Vector(0, 0), new Vector(10, 10));
        assertFalse(w.contains(new Vector(0, 10), 5));
        assertFalse(w.contains(new Vector(10, 0), 5));
        assertFalse(w.contains(new Vector(16, 16), 5));
        assertFalse(w.contains(new Vector(-6, -6), 5));

        assertTrue(w.contains(new Vector(5, 5), 5));
        assertTrue(w.contains(new Vector(6, 4), 5));
        assertTrue(w.contains(new Vector(7, 3), 5));
        assertTrue(w.contains(new Vector(8, 2), 5));
        assertFalse(w.contains(new Vector(9, 1), 5));

        assertTrue(w.contains(new Vector(4, 6), 5));
        assertTrue(w.contains(new Vector(3, 7), 5));
        assertTrue(w.contains(new Vector(2, 8), 5));
        assertFalse(w.contains(new Vector(1, 9), 5));


        assertTrue(w.contains(new Vector(6, 4), 3));
        assertTrue(w.contains(new Vector(7, 3), 3));
        assertFalse(w.contains(new Vector(8, 2), 3));

        assertTrue(w.contains(new Vector(4, 6), 3));
        assertTrue(w.contains(new Vector(3, 7), 3));
        assertFalse(w.contains(new Vector(2, 8), 3));
    }


}
