/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics;

/**
 * A instance that performs a modification of styles on the drawing and then draws it on a given delegate.
 */
public abstract class GraphicRestyle extends Graphic {

    private final Graphic parent;

    /**
     * Creates a new instace
     *
     * @param parent the delegate to be used to berform the drawing
     */
    public GraphicRestyle(Graphic parent) {
        this.parent = parent;
    }

    /**
     * The function to modify the style
     *
     * @param style the old style
     * @return the new style
     */
    public abstract Style getStyle(Style style);

    @Override
    public void drawLine(VectorInterface p1, VectorInterface p2, Style style) {
        parent.drawLine(p1, p2, getStyle(style));
    }


    @Override
    public void drawPolygon(Polygon p, Style style) {
        parent.drawPolygon(p, getStyle(style));
    }

    @Override
    public void drawCircle(VectorInterface p1, VectorInterface p2, Style style) {
        parent.drawCircle(p1, p2, getStyle(style));
    }

    @Override
    public void drawText(VectorInterface p1, VectorInterface p2, VectorInterface p3, String text, Orientation orientation, Style style) {
        parent.drawText(p1, p2, p3, text, orientation, getFontStyle(style));
    }

    /**
     * The function to modify the font style.
     * By default, it calls getStyle.
     *
     * @param style the old style
     * @return the new style
     */
    public Style getFontStyle(Style style) {
        return getStyle(style);
    }

    @Override
    public boolean isFlagSet(Flag flag) {
        return parent.isFlagSet(flag);
    }
}
