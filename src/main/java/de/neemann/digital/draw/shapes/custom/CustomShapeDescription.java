/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes.custom;

import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.graphics.*;
import de.neemann.digital.draw.graphics.Polygon;
import de.neemann.digital.draw.shapes.Drawable;
import de.neemann.digital.lang.Lang;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;

/**
 * Is intended to be stored in a file.
 */
public class CustomShapeDescription implements Iterable<Drawable> {
    /**
     * The default empty shape instance
     */
    public static final CustomShapeDescription EMPTY = new CustomShapeDescription();

    private HashMap<String, Pin> pins;
    private ArrayList<Drawable> drawables;

    /**
     * Creates a new instance
     */
    public CustomShapeDescription() {
        pins = new HashMap<>();
        drawables = new ArrayList<>();
    }

    /**
     * Adds a pin to this shape description
     *
     * @param name      the name of the pin
     * @param pos       the pins position
     * @param showLabel if true the label of the pin is shown
     * @return this for chained calls
     */
    public CustomShapeDescription addPin(String name, Vector pos, boolean showLabel) {
        pins.put(name, new Pin(pos, showLabel));
        return this;
    }


    /**
     * Adds a polygon to the shape
     *
     * @param p1        starting point of the line
     * @param p2        ending point of the line
     * @param thickness the line thickness
     * @param color     the color to use
     * @return this for chained calls
     */
    public CustomShapeDescription addLine(Vector p1, Vector p2, int thickness, Color color) {
        drawables.add(new LineHolder(p1, p2, thickness, color));
        return this;
    }

    /**
     * Adds a circle to the shape
     *
     * @param p1        upper left corner of the circles bounding box
     * @param p2        lower right corner of the circles bounding box
     * @param thickness the line thickness
     * @param color     the color to use
     * @param filled    true if filled
     * @return this for chained calls
     */
    public CustomShapeDescription addCircle(Vector p1, Vector p2, int thickness, Color color, boolean filled) {
        drawables.add(new CircleHolder(p1, p2, thickness, color, filled));
        return this;
    }

    /**
     * Adds a polygon to the shape
     *
     * @param poly      the polygon to add
     * @param thickness the line thickness
     * @param color     the color to use
     * @param filled    true if filled
     * @return this for chained calls
     */
    public CustomShapeDescription addPolygon(Polygon poly, int thickness, Color color, boolean filled) {
        drawables.add(new PolygonHolder(poly, thickness, filled, color));
        return this;
    }

    /**
     * Adds a text to the shape
     *
     * @param p1          position
     * @param p2          second position to determin the base line orientation
     * @param text        the text to draw
     * @param orientation the orientation of the text
     * @param size        the font size
     * @param color       the text color
     * @return this for chained calls
     */
    public CustomShapeDescription addText(Vector p1, Vector p2, String text, Orientation orientation, int size, Color color) {
        drawables.add(new TextHolder(p1, p2, text, orientation, size, color));
        return this;
    }

    /**
     * Returns the position of the given pin.
     *
     * @param name the name of the pin
     * @return the position of the pin
     * @throws PinException thrown if pin is not found
     */
    Pin getPin(String name) throws PinException {
        final Pin pin = pins.get(name);
        if (pin == null)
            throw new PinException(Lang.get("err_pin_N_notFound", name));
        return pin;
    }

    @Override
    public Iterator<Drawable> iterator() {
        return drawables.iterator();
    }

    /**
     * Creates a simple dummy shape
     *
     * @return the dummy shape
     */
    public static CustomShapeDescription createDummy() {
        return new CustomShapeDescription()
                .addPin("A", new Vector(0, 0), true)
                .addPin("B", new Vector(0, SIZE * 2), true)
                .addPin("Y", new Vector(SIZE * 3, SIZE), true)
                .addCircle(new Vector(0, -SIZE2), new Vector(SIZE * 3, SIZE * 3 - SIZE2), Style.NORMAL.getThickness(), Color.BLACK, false)
                .addPolygon(Polygon.createFromPath("m 20,5 c 30 0 0 30 -30 0 z"), Style.NORMAL.getThickness(), Color.BLACK, false)
                .addLine(new Vector(0, -SIZE2), new Vector(SIZE * 3, SIZE * 3 - SIZE2), Style.NORMAL.getThickness(), Color.BLACK)
                .addText(new Vector(20, -25), new Vector(21, -25), "Hi!", Orientation.LEFTCENTER, 20, Color.BLACK);
    }

    /**
     * Stores a line.
     */
    public static final class LineHolder implements Drawable {
        private final Vector p1;
        private final Vector p2;
        private final int thickness;
        private final Color color;

        private LineHolder(Vector p1, Vector p2, int thickness, Color color) {
            this.p1 = p1;
            this.p2 = p2;
            this.thickness = thickness;
            this.color = color;
        }

        @Override
        public void drawTo(Graphic graphic, Style highLight) {
            graphic.drawLine(p1, p2, Style.NORMAL.deriveStyle(thickness, false, color));
        }
    }

    /**
     * Stores a circle
     */
    public static final class CircleHolder implements Drawable {
        private final Vector p1;
        private final Vector p2;
        private final int thickness;
        private final Color color;
        private final boolean filled;


        private CircleHolder(Vector p1, Vector p2, int thickness, Color color, boolean filled) {
            this.p1 = p1;
            this.p2 = p2;
            this.thickness = thickness;
            this.color = color;
            this.filled = filled;
        }

        @Override
        public void drawTo(Graphic graphic, Style highLight) {
            graphic.drawCircle(p1, p2, Style.NORMAL.deriveStyle(thickness, filled, color));
        }
    }

    /**
     * Stores a polygon
     */
    public static final class PolygonHolder implements Drawable {
        private final Polygon poly;
        private final int thickness;
        private final boolean filled;
        private final Color color;

        private PolygonHolder(Polygon poly, int thickness, boolean filled, Color color) {
            this.poly = poly;
            this.thickness = thickness;
            this.filled = filled;
            this.color = color;
        }

        @Override
        public void drawTo(Graphic graphic, Style highLight) {
            graphic.drawPolygon(poly, Style.NORMAL.deriveStyle(thickness, filled, color));
        }
    }

    /**
     * Stores a text
     */
    public static final class TextHolder implements Drawable {
        private final Vector p1;
        private final Vector p2;
        private final String text;
        private final Orientation orientation;
        private final int size;
        private final Color color;

        private TextHolder(Vector p1, Vector p2, String text, Orientation orientation, int size, Color color) {
            this.p1 = p1;
            this.p2 = p2;
            this.text = text;
            this.orientation = orientation;
            this.size = size;
            this.color = color;
        }

        @Override
        public void drawTo(Graphic graphic, Style highLight) {
            graphic.drawText(p1, p2, text, orientation,
                    Style.NORMAL
                            .deriveFontStyle(size, true)
                            .deriveColor(color));
        }
    }

    /**
     * Describes a pin position
     */
    public static final class Pin {
        private Vector pos;
        private boolean showLabel;

        private Pin(Vector pos, boolean showLabel) {
            this.pos = pos;
            this.showLabel = showLabel;
        }

        boolean isShowLabel() {
            return showLabel;
        }

        Vector getPos() {
            return pos;
        }
    }
}
