/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics;

import junit.framework.TestCase;

public class PolygonTest extends TestCase {

    public void testPath() {
        checkLine(Polygon.createFromPath("m 10,10 L 20,10 20,20 10,20 Z"));
        checkLine(Polygon.createFromPath("m 10,10 l 10,0 0,10 -10,0 Z"));
        checkLine(Polygon.createFromPath("m 10,10 h 10 v 10 h -10 Z"));
        checkLine(Polygon.createFromPath("m 10,10 H 20 V 20 H 10 Z"));
    }

    private void checkLine(Polygon p) {
        assertNotNull(p);
        assertEquals("M 10,10 L 20,10 L 20,20 L 10,20 Z", p.toString());
    }

    private void checkBezier(Polygon p) {
        assertEquals("M 10,10 C 20,10 20,20 10,20 Z", p.toString());
    }

    public void testBezierPath() {
        checkBezier(Polygon.createFromPath("m 10,10 C 20 10 20 20 10 20 Z"));
        checkBezier(Polygon.createFromPath("m 10,10 c 10 0 10 10 0 10 Z"));
    }

    public void testCubicReflect() {
        assertEquals("M 0,0 C 0,10 10,10 10,0 C 10,-10 20,-10 20,0 C 20,10 30,10 30,0 Z",
                Polygon.createFromPath("m 0,0 c 0,10 10,10 10,0 s 10,-10 10,0 s 10,10 10,0 Z").toString());
    }

    public void testQuadraticReflect() {
        assertEquals("M 0,0 Q 30,30 60,0 Q 90,-30 120,0 Q 150,30 180,0 Q 210,-30 240,0",
                Polygon.createFromPath("m 0,0 Q 30,30 60,0 t 60,0 t 60,0 t 60,0").toString());
    }

    public void testMultiPath() {
        assertEquals("M 0,0 Q 0,60 60,60 Q 120,60 120,0 Q 120,-60 60,-60 Q 0,-60 0,0 Z M 30,0 Q 30,30 60,30 Q 90,30 90,0 Q 90,-30 60,-30 Q 30,-30 30,0 Z",
                Polygon.createFromPath("M 0,0 Q 0,60 60,60 T 120,0 T 60,-60 T 0,0 z M 30,0 Q 30,30 60,30 T 90,0 T 60,-30 T 30,0 Z").toString());
    }

    public void testAppend() {
        Polygon p1 = new Polygon(false).add(0, 0).add(0, 10).add(10, 10);
        Polygon p2 = new Polygon(false).add(10, 10).add(10, 0).add(0, 0);

        assertEquals("M 0,0 L 0,10 L 10,10 L 10,0 Z", p1.append(p2).toString());
    }

}