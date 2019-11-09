/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics.linemerger;

import de.neemann.digital.draw.graphics.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Merges all single lines which are drawn to polygons.
 * Needed to create a nicer svg export because of the creation of longer strokes
 * instead of single lines.
 */
public class GraphicLineCollector extends Graphic {
    private final HashMap<Style, PolygonSet> polys;

    /**
     * Creates a new instance
     */
    public GraphicLineCollector() {
        this.polys = new HashMap<>();
    }

    @Override
    public void drawLine(VectorInterface p1, VectorInterface p2, Style style) {
        PolygonSet polyList = polys.get(style);
        if (polyList == null) {
            polyList = new PolygonSet(style);
            polys.put(style, polyList);
        }
        polyList.add(p1, p2);
    }

    @Override
    public void drawPolygon(Polygon p, Style style) {
    }

    @Override
    public void drawCircle(VectorInterface p1, VectorInterface p2, Style style) {
    }

    @Override
    public void drawText(VectorInterface p1, VectorInterface p2, VectorInterface p3, String text, Orientation orientation, Style style) {
    }

    private static final class PolygonSet implements Iterable<Polygon> {
        private final ArrayList<Polygon> polyList;
        private final Style style;

        private PolygonSet(Style style) {
            this.style = style;
            this.polyList = new ArrayList<>();
        }

        private void add(VectorInterface p1, VectorInterface p2) {
            for (Polygon p : polyList) {
                if (p.addLine(p1, p2)) {
                    tryMerge(p);
                    return;
                }
            }
            Polygon p = new Polygon(false).add(p1).add(p2);
            polyList.add(p);
        }

        @Override
        public Iterator<Polygon> iterator() {
            return polyList.iterator();
        }

        private void tryMerge(Polygon p1) {
            for (Polygon p2 : polyList)
                if (p1 != p2 && !p1.isClosed() && !p2.isClosed()) {
                    if (p1.getLast().equals(p2.getFirst())) {
                        p1.append(p2);
                        polyList.remove(p2);
                        return;
                    } else if (p2.getLast().equals(p1.getFirst())) {
                        p2.append(p1);
                        polyList.remove(p1);
                        return;
                    } else if (p1.getLast().equals(p2.getLast())) {
                        p1.append(p2.reverse());
                        polyList.remove(p2);
                        return;
                    } else if (p1.getFirst().equals(p2.getFirst())) {
                        polyList.remove(p1);
                        polyList.remove(p2);
                        polyList.add(p1.reverse().append(p2));
                        return;
                    }
                }
        }

        public void drawTo(Graphic gr) {
            for (Polygon p : polyList)
                gr.drawPolygon(p, style);
        }
    }

    /**
     * Draws the polygons to the given {@link Graphic} instance
     *
     * @param gr the {@link Graphic} instace to use
     */
    public void drawTo(Graphic gr) {
        for (PolygonSet p : polys.values())
            p.drawTo(gr);
    }
}
