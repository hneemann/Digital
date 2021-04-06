/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics;

import de.neemann.digital.draw.graphics.text.formatter.GraphicsFormatter;

import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;

import static de.neemann.digital.draw.graphics.GraphicSwing.getMirrorYOrientation;

/**
 * This class is used to determine the size of shapes or the whole circuit.
 * You can draw the items to an instance of this class and then obtain the size
 * by the getters getMin() and getMax().
 */
public class GraphicMinMax extends Graphic {

    private final boolean includeText;
    private final Graphic parent;
    private Vector min;
    private Vector max;

    /**
     * Creates a new instance
     */
    public GraphicMinMax() {
        this(true, null);
    }

    /**
     * Creates a new instance
     *
     * @param parent oly used to provide the flags
     */
    public GraphicMinMax(Graphic parent) {
        this(true, parent);
    }

    /**
     * Creates a new instance
     *
     * @param includeText true if text is included in measurement
     * @param parent      oly used to provide the flags
     */
    public GraphicMinMax(boolean includeText, Graphic parent) {
        this.includeText = includeText;
        this.parent = parent;
    }

    @Override
    public void drawLine(VectorInterface p1, VectorInterface p2, Style style) {
        check(p1);
        check(p2);
    }

    @Override
    public void drawPolygon(Polygon p, Style style) {
        p.traverse(this::check);
    }

    @Override
    public void drawCircle(VectorInterface p1, VectorInterface p2, Style style) {
        check(p1);
        check(p2);
    }

    /**
     * Checks the given point and makes the bounding box larger if necessary.
     *
     * @param p the point to check
     */
    public void check(VectorInterface p) {
        if (min == null || max == null) {
            min = new Vector(p.getX(), p.getY());
            max = new Vector(p.getX(), p.getY());
        } else {
            min = Vector.min(min, p);
            max = Vector.max(max, p);
        }
    }

    @Override
    public void drawText(VectorInterface p1, VectorInterface p2, VectorInterface p3, String text, Orientation orientation, Style style) {
        if (includeText || style.mattersAlwaysForSize())
            approxTextSize(this, p1, p2, p3, text, orientation, style);
    }

    /**
     * Approximation of text size
     *
     * @param gr          the Graphic instance to use
     * @param p1          point to draw the text
     * @param p2          at the left of p1, is used to determine the correct orientation of the text after transforming coordinates
     * @param p3          at the top of p1, is used to determine the correct orientation of the text after transforming coordinates
     * @param text        the text
     * @param orientation the texts orientation
     * @param style       the text style
     */
    public static void approxTextSize(Graphic gr, VectorInterface p1, VectorInterface p2, VectorInterface p3, String text, Orientation orientation, Style style) {
        if (text != null && text.length() > 0) {
            VectorFloat delta = p2.sub(p1).norm();
            VectorFloat height = new VectorFloat(delta.getYFloat(), -delta.getXFloat()).mul(style.getFontSize());

            int textWidth = getTextWidth(text, style);
            VectorFloat width = delta.mul(textWidth);

            VectorInterface p = p1;
            if (orientation.getX() != 0) {
                p = p.sub(width.mul(orientation.getX()).div(2));
            }

            int oy = getMirrorYOrientation(orientation, p1, p2, p3);
            if (oy != 0) {
                p = p.sub(height.mul(oy).div(2));
            } else
                p = p.sub(height.div(4));

            gr.drawPolygon(new Polygon(true)
                    .add(p)
                    .add(p.add(width))
                    .add(p.add(width).add(height))
                    .add(p.add(height)), Style.THIN);
        }
    }

    /**
     * Returns a approximation of the width of the given text in the given style
     *
     * @param text  the text
     * @param style the style
     * @return the approximated text width
     */
    public static int getTextWidth(String text, Style style) {
        final FontRenderContext fontRenderContext = new FontRenderContext(null, true, false);
        GraphicsFormatter.Fragment f = GraphicsFormatter.createFragment((fragment, font, str) -> {
            Rectangle2D rec = style.getFont().getStringBounds(str, fontRenderContext);
            fragment.set((int) rec.getWidth(), (int) rec.getHeight(), 0);
        }, style.getFont(), text);
        return f.getWidth();
    }

    /**
     * @return the upper left corner of the circuit
     */
    public Vector getMin() {
        return min;
    }

    /**
     * @return the lower right corner of the circuit
     */
    public Vector getMax() {
        return max;
    }

    @Override
    public boolean isFlagSet(Flag flag) {
        if (parent == null)
            return false;
        else
            return parent.isFlagSet(flag);
    }

    /**
     * @return true if this instance is valid
     */
    public boolean isValid() {
        return min != null && max != null;
    }

    @Override
    public String toString() {
        return "GraphicMinMax{"
                + "min=" + min
                + ", max=" + max + '}';
    }
}
