/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics.linemerger;

import de.neemann.digital.draw.graphics.*;
import junit.framework.TestCase;

import java.util.ArrayList;

public class GraphicLineCollectorTest extends TestCase {

    public void testOpenSquare() {
        GraphicLineCollector col = new GraphicLineCollector();
        col.drawLine(new Vector(0, 0), new Vector(10, 0), Style.NORMAL);
        col.drawLine(new Vector(0, 10), new Vector(10, 10), Style.NORMAL);

        col.drawLine(new Vector(0, 0), new Vector(0, 10), Style.NORMAL);

        ArrayList<Polygon> poly = new ArrayList<>();
        col.drawTo(new MyGraphic(poly));

        assertEquals(1, poly.size());
        assertEquals("M 10,0 L 0,0 L 0,10 L 10,10", poly.get(0).toString());
    }

    public void testClosedSquare() {
        GraphicLineCollector col = new GraphicLineCollector();
        col.drawLine(new Vector(0, 0), new Vector(10, 0), Style.NORMAL);
        col.drawLine(new Vector(0, 10), new Vector(10, 10), Style.NORMAL);

        col.drawLine(new Vector(0, 0), new Vector(0, 10), Style.NORMAL);
        col.drawLine(new Vector(10, 0), new Vector(10, 10), Style.NORMAL);

        ArrayList<Polygon> poly = new ArrayList<>();
        col.drawTo(new MyGraphic(poly));

        assertEquals(1, poly.size());
        assertEquals("M 10,0 L 0,0 L 0,10 L 10,10 Z", poly.get(0).toString());
    }

    public void testClosedSquare2() {
        GraphicLineCollector col = new GraphicLineCollector();
        col.drawLine(new Vector(0, 0), new Vector(10, 0), Style.NORMAL);
        col.drawLine(new Vector(0, 10), new Vector(10, 10), Style.NORMAL);

        col.drawLine(new Vector(0, 0), new Vector(0, 10), Style.NORMAL);
        col.drawLine(new Vector(10, 0), new Vector(10, 10), Style.NORMAL);

        ArrayList<Polygon> poly = new ArrayList<>();
        col.drawTo(new MyGraphic(poly));

        assertEquals(1, poly.size());
        assertEquals("M 10,0 L 0,0 L 0,10 L 10,10 Z", poly.get(0).toString());
    }

    private static class MyGraphic extends Graphic {
        private final ArrayList<Polygon> poly;

        private MyGraphic(ArrayList<Polygon> poly) {
            this.poly = poly;
        }

        @Override
        public void drawLine(VectorInterface p1, VectorInterface p2, Style style) {
        }

        @Override
        public void drawPolygon(Polygon p, Style style) {
            poly.add(p);
        }

        @Override
        public void drawCircle(VectorInterface p1, VectorInterface p2, Style style) {
        }

        @Override
        public void drawText(VectorInterface p1, VectorInterface p2, VectorInterface p3, String text, Orientation orientation, Style style) {
        }
    }
}