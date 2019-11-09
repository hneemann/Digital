/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics;

/**
 * A instance that performs a transformation on the drawing and then draws it on a given delegate.
 */
public class GraphicTransform extends Graphic {

    private final Graphic parent;
    private final Transform transform;

    /**
     * Creates a new instace
     *
     * @param parent    the delegate to be used to berform the drawing
     * @param transform the transformation
     */
    public GraphicTransform(Graphic parent, Transform transform) {
        this.parent = parent;
        this.transform = transform;
    }

    @Override
    public void drawLine(VectorInterface p1, VectorInterface p2, Style style) {
        parent.drawLine(p1.transform(transform), p2.transform(transform), style);
    }

    @Override
    public void drawPolygon(Polygon p, Style style) {
        parent.drawPolygon(p.transform(transform), style);
    }

    @Override
    public void drawCircle(VectorInterface p1, VectorInterface p2, Style style) {
        parent.drawCircle(p1.transform(transform), p2.transform(transform), style);
    }

    @Override
    public void drawText(VectorInterface p1, VectorInterface p2, VectorInterface p3, String text, Orientation orientation, Style style) {
        parent.drawText(p1.transform(transform), p2.transform(transform), p3.transform(transform), text, orientation, style);
    }

    @Override
    public boolean isFlagSet(Flag flag) {
        return parent.isFlagSet(flag);
    }
}
