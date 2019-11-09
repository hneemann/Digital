/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics;

import java.io.Closeable;
import java.io.IOException;

/**
 * Interface used to draw the circuit.
 * There are implementations to draw on a {@link java.awt.Graphics2D} instance ({@link GraphicSwing}) but also
 * implementations which create export formats like SVG ({@link GraphicSVG}).
 */
public abstract class Graphic implements Closeable {

    /**
     * The available flags
     */
    public enum Flag {noShapeFilling, smallIO, hideTest, noPinMarker, thinnerLines, tiny}

    /**
     * Sets the bounding box of the future usage of this instance
     * Instances that create a file will use this bounding box th write a header.
     * So this method needs to be called before a draw-Method is called.
     *
     * @param min upper left corner
     * @param max lower right corner
     * @return this for chained calls
     */
    public Graphic setBoundingBox(VectorInterface min, VectorInterface max) {
        return this;
    }

    /**
     * Draws a line
     *
     * @param p1    first point
     * @param p2    second point
     * @param style the line style
     */
    public abstract void drawLine(VectorInterface p1, VectorInterface p2, Style style);

    /**
     * Draws a polygon
     *
     * @param p     the polygon to draw
     * @param style the style
     */
    public abstract void drawPolygon(Polygon p, Style style);

    /**
     * Draws a circle
     *
     * @param p1    upper left corner of outer rectangle containing the circle
     * @param p2    lower right corner of outer rectangle containing the circle
     * @param style the style
     */
    public abstract void drawCircle(VectorInterface p1, VectorInterface p2, Style style);

    /**
     * Draws text
     *
     * @param p1          point to draw the text
     * @param p2          point at the left of p1, is used to determine the correct orientation of the text after transforming coordinates
     * @param p3          point at the top of p1, is used to determine the correct orientation of the text after transforming coordinates
     * @param text        the text
     * @param orientation the text orientation
     * @param style       the text style
     */
    public abstract void drawText(VectorInterface p1, VectorInterface p2, VectorInterface p3, String text, Orientation orientation, Style style);

    /**
     * Draws text
     *
     * @param p1          point to draw the text
     * @param p2          point at the left of p1, is used to determine the correct orientation of the text after transforming coordinates
     * @param text        the text
     * @param orientation the text orientation
     * @param style       the text style
     */
    public final void drawText(VectorInterface p1, VectorInterface p2, String text, Orientation orientation, Style style) {
        VectorInterface d = p2.sub(p1).toFloat().getOrthogonal();
        drawText(p1, p2, p1.add(d), text, orientation, style);
    }

    /**
     * Helper to draw a horizontal left to right text
     *
     * @param pos         the text position
     * @param text        the text
     * @param orientation the text orientation
     * @param style       the text style
     */
    public final void drawText(VectorInterface pos, String text, Orientation orientation, Style style) {
        drawText(pos, pos.add(new Vector(1, 0)), text, orientation, style);
    }

    /**
     * opens a new group, used to create SVG grouping
     */
    public void openGroup() {
    }

    /**
     * closes a group, used to create SVG grouping
     */
    public void closeGroup() {
    }

    /**
     * Returns true if the given flag is set
     *
     * @param flag the flag
     * @return true if the given flag is set
     */
    public boolean isFlagSet(Flag flag) {
        return false;
    }

    /**
     * closes the graphics instance
     *
     * @throws IOException IOException
     */
    public void close() throws IOException {
    }
}
