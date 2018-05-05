/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics;

import junit.framework.TestCase;

public class PolygonTest extends TestCase {

    public void testPath() {
        checkLine(Polygon.createFromPath("m 10,10 L 20,10 20,20 10,20 z"));
        checkLine(Polygon.createFromPath("m 10,10 l 10,0 0,10 -10,0 z"));
        checkLine(Polygon.createFromPath("m 10,10 h 10 v 10 h -10 z"));
        checkLine(Polygon.createFromPath("m 10,10 H 20 V 20 H 10 z"));
    }

    private void checkLine(Polygon p) {
        checkCoor(p);
        assertEquals("M 10.0 10.0 L 20.0 10.0 L 20.0 20.0 L 10.0 20.0 z", p.toString());
    }

    private void checkCoor(Polygon p) {
        assertEquals(4, p.size());
        assertEquals(new VectorFloat(10, 10), p.get(0));
        assertEquals(new VectorFloat(20, 10), p.get(1));
        assertEquals(new VectorFloat(20, 20), p.get(2));
        assertEquals(new VectorFloat(10, 20), p.get(3));
    }

    private void checkBezier(Polygon p) {
        checkCoor(p);
        assertEquals("M 10.0 10.0 C 20.0 10.0 20.0 20.0 10.0 20.0 z", p.toString());
    }

    public void testBezierPath() {
        checkBezier(Polygon.createFromPath("m 10,10 C 20 10 20 20 10 20 z"));
        checkBezier(Polygon.createFromPath("m 10,10 c 10 0 0 10 -10 0 z"));
    }
}