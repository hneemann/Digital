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
        assertEquals("M 10,10 L 20,10 L 20,20 L 10,20 z", p.toString());
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
        assertEquals("M 10,10 C 20,10 20,20 10,20 z", p.toString());
    }

    public void testBezierPath() {
        checkBezier(Polygon.createFromPath("m 10,10 C 20 10 20 20 10 20 z"));
        checkBezier(Polygon.createFromPath("m 10,10 c 10 0 10 10 0 10 z"));
    }

    public void testCubicReflect() {
        assertEquals("M 0,0 C 0,10 10,10 10,0 C 10,-10 20,-10 20,0 C 20,10 30,10 30,0 z",
                Polygon.createFromPath("m 0,0 c 0,10 10,10 10,0 s 10,-10 10,0 s 10,10 10,0 z").toString());
    }

    public void testQuadraticReflect() {
        assertEquals("M 0,0 C 20,20 40,20 60,0 C 80,-20 100,-20 120,0 C 140,20 160,20 180,0 C 200,-20 220,-20 240,0 ",
                Polygon.createFromPath("m 0,0 Q 30,30 60,0 t 60,0 t 60,0 t 60,0").toString());
    }
}