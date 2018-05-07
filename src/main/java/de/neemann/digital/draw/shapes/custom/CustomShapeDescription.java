
/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes.custom;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;

import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Orientation;
import de.neemann.digital.draw.graphics.Polygon;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.draw.graphics.VectorInterface;
import de.neemann.digital.draw.graphics.svg.SVGPseudoPin;
import de.neemann.digital.draw.shapes.Drawable;
import de.neemann.digital.lang.Lang;

/**
 * Is intended to be stored in a file.
 */
public class CustomShapeDescription implements Iterable<Drawable> {
    /**
     * The default empty shape instance
     */
    public static final CustomShapeDescription EMPTY = new CustomShapeDescription();

    private ArrayList<SVGPseudoPin> pins;
    private ArrayList<Drawable> drawables;

    /**
     * Creates a new instance
     */
    public CustomShapeDescription() {
        pins = new ArrayList<>();
        drawables = new ArrayList<>();
    }

    /**
     * Adds a pin to this shape description
     * @param name
     *            the name of the pin
     * @param pos
     *            the pins position
     * @param input
     *            whether its an input Pin
     * @return this for chained calls
     */
    public CustomShapeDescription addPin(String name, VectorInterface pos, boolean input) {
        pins.add(new SVGPseudoPin(pos, name, input, null));
        return this;
    }

    /**
     * Adds a given Pin
     * @param p
     *            Pin
     * @return this for chained calls
     */
    public CustomShapeDescription addPin(SVGPseudoPin p) {
        pins.add(p);
        return this;
    }

    /**
     * Adds a polygon to the shape
     * @param p1
     *            starting point of the line
     * @param p2
     *            ending point of the line
     * @param thickness
     *            the line thickness
     * @param color
     *            the color to use
     * @return this for chained calls
     */
    public CustomShapeDescription addLine(VectorInterface p1, VectorInterface p2, int thickness,
            Color color) {
        drawables.add(new LineHolder(p1, p2, thickness, color));
        return this;
    }

    /**
     * Adds a polygon to the shape
     * @param p1
     *            starting point of the line
     * @param p2
     *            ending point of the line
     * @param style
     *            style
     * @return this for chained calls
     */
    public CustomShapeDescription addLine(VectorInterface p1, VectorInterface p2, Style style) {
        drawables.add(new LineHolder(p1, p2, style));
        return this;
    }

    /**
     * Adds a circle to the shape
     * @param p1
     *            upper left corner of the circles bounding box
     * @param p2
     *            lower right corner of the circles bounding box
     * @param thickness
     *            the line thickness
     * @param color
     *            the color to use
     * @param filled
     *            true if filled
     * @return this for chained calls
     */
    public CustomShapeDescription addCircle(VectorInterface p1, VectorInterface p2, int thickness,
            Color color, boolean filled) {
        drawables.add(new CircleHolder(p1, p2, thickness, color, filled));
        return this;
    }

    /**
     * Adds a circle to the shape
     * @param p1
     *            upper left corner of the circles bounding box
     * @param p2
     *            lower right corner of the circles bounding box
     * @param style
     *            style
     * @return this for chained calls
     */
    public CustomShapeDescription addCircle(VectorInterface p1, VectorInterface p2, Style style) {
        drawables.add(new CircleHolder(p1, p2, style));
        return this;
    }

    /**
     * Adds a polygon to the shape
     * @param poly
     *            the polygon to add
     * @param thickness
     *            the line thickness
     * @param color
     *            the color to use
     * @param filled
     *            true if filled
     * @return this for chained calls
     */
    public CustomShapeDescription addPolygon(Polygon poly, int thickness, Color color,
            boolean filled) {
        drawables.add(new PolygonHolder(poly, thickness, filled, color));
        return this;
    }

    /**
     * Adds a polygon
     * @param poly
     *            polygon
     * @param style
     *            Styleof the polygon
     * @return this
     */
    public CustomShapeDescription addPolygon(Polygon poly, Style style) {
        drawables.add(new PolygonHolder(poly, style));
        return this;
    }

    /**
     * Adds a text to the shape
     * @param p1
     *            position
     * @param p2
     *            second position to determin the base line orientation
     * @param text
     *            the text to draw
     * @param orientation
     *            the orientation of the text
     * @param size
     *            the font size
     * @param color
     *            the text color
     * @return this for chained calls
     */
    public CustomShapeDescription addText(VectorInterface p1, VectorInterface p2, String text,
            Orientation orientation, int size, Color color) {
        drawables.add(new TextHolder(p1, p2, text, orientation, size, color));
        return this;
    }

    /**
     * Adds a text to the shape
     * @param p1
     *            position
     * @param p2
     *            second position to determin the base line orientation
     * @param text
     *            the text to draw
     * @param orientation
     *            the orientation of the text
     * @param style
     *            style
     * @return this for chained calls
     */
    public CustomShapeDescription addText(VectorInterface p1, VectorInterface p2, String text,
            Orientation orientation, Style style) {
        drawables.add(new TextHolder(p1, p2, text, orientation, style));
        return this;
    }

    /**
     * Returns the position of the given pin.
     * @param name
     *            the name of the pin
     * @return the pin
     * @throws PinException
     *             thrown if pin is not found
     */
    Pin getPin(String name) throws PinException {
        for (SVGPseudoPin p : pins)
            if (p.getLabel().equals(name))
                return new Pin(p.getPos(), true);
        throw new PinException(Lang.get("err_pin_N_notFound", name));
    }

    /**
     * Get the names of all integrated Pins
     * @return list of Pins
     */
    public SVGPseudoPin[] getPinNames() {
        return pins.toArray(new SVGPseudoPin[pins.size()]);
    }

    /**
     * Removes a Pin and adds it with a new Position
     * @param fresh
     *            new Position
     * @param label
     *            Name of the Pin
     * @return this for chained calls
     * @throws PinException
     *             if the Pin is not found
     */
    public CustomShapeDescription transformPin(Vector fresh, String label) throws PinException {
        for (SVGPseudoPin p : pins)
            if (p.getLabel().equals(label))
                p.setPos(fresh);
        return this;
    }

    @Override
    public Iterator<Drawable> iterator() {
        return drawables.iterator();
    }

    /**
     * Creates a simple dummy shape
     * @return the dummy shape
     */
    public static CustomShapeDescription createDummy() {
        return new CustomShapeDescription().addPin("A", new Vector(0, 0), true)
                .addPin("B", new Vector(0, SIZE * 2), true)
                .addPin("Y", new Vector(SIZE * 3, SIZE), true)
                .addCircle(new Vector(0, -SIZE2), new Vector(SIZE * 3, SIZE * 3 - SIZE2),
                        Style.NORMAL.getThickness(), Color.BLACK, false)
                .addPolygon(Polygon.createFromPath("m 20,5 c 30 0 0 30 -30 0 z"),
                        Style.NORMAL.getThickness(), Color.BLACK, false)
                .addLine(new Vector(0, -SIZE2), new Vector(SIZE * 3, SIZE * 3 - SIZE2),
                        Style.NORMAL.getThickness(), Color.BLACK)
                .addText(new Vector(20, -25), new Vector(21, -25), "Hi!", Orientation.LEFTCENTER,
                        20, Color.BLACK);
    }

    /**
     * Stores a line.
     */
    public static final class LineHolder implements Drawable {
        private final VectorInterface p1;
        private final VectorInterface p2;
        private final Style style;

        private LineHolder(VectorInterface p1, VectorInterface p2, int thickness, Color color) {
            this.p1 = p1;
            this.p2 = p2;
            style = Style.NORMAL.deriveStyle(thickness, false, color);
        }

        private LineHolder(VectorInterface p1, VectorInterface p2, Style style) {
            this.p1 = p1;
            this.p2 = p2;
            this.style = style;
        }

        @Override
        public void drawTo(Graphic graphic, Style highLight) {
            graphic.drawLine(p1, p2, style);
        }
    }

    /**
     * Stores a circle
     */
    public static final class CircleHolder implements Drawable {
        private final VectorInterface p1;
        private final VectorInterface p2;
        private final Style style;

        private CircleHolder(VectorInterface p1, VectorInterface p2, int thickness, Color color,
                boolean filled) {
            this.p1 = p1;
            this.p2 = p2;
            style = Style.NORMAL.deriveStyle(thickness, filled, color);
        }

        private CircleHolder(VectorInterface p1, VectorInterface p2, Style style) {
            this.p1 = p1;
            this.p2 = p2;
            this.style = style;
        }

        @Override
        public void drawTo(Graphic graphic, Style highLight) {
            graphic.drawCircle(p1, p2, style);
        }
    }

    /**
     * Stores a polygon
     */
    public static final class PolygonHolder implements Drawable {
        private final Polygon poly;
        private Style style;

        private PolygonHolder(Polygon poly, int thickness, boolean filled, Color color) {
            this.poly = poly;
            style = Style.NORMAL.deriveStyle(thickness, filled, color);
        }

        private PolygonHolder(Polygon poly, Style style) {
            this.poly = poly;
            this.style = style;
        }

        @Override
        public void drawTo(Graphic graphic, Style highLight) {
            graphic.drawPolygon(poly, style);
        }
    }

    /**
     * Stores a text
     */
    public static final class TextHolder implements Drawable {
        private final VectorInterface p1;
        private final VectorInterface p2;
        private final String text;
        private final Orientation orientation;
        private final Style style;

        private TextHolder(VectorInterface p1, VectorInterface p2, String text,
                Orientation orientation, int size, Color color) {
            this.p1 = p1;
            this.p2 = p2;
            this.text = text;
            this.orientation = orientation;
            style = Style.NORMAL.deriveFontStyle(size, true).deriveColor(color);
        }

        private TextHolder(VectorInterface p1, VectorInterface p2, String text,
                Orientation orientation, Style style) {
            this.p1 = p1;
            this.p2 = p2;
            this.text = text;
            this.orientation = orientation;
            this.style = style;
        }

        @Override
        public void drawTo(Graphic graphic, Style highLight) {
            graphic.drawText(p1, p2, text, orientation, style);
        }
    }

    /**
     * Describes a pin position
     */
    public static final class Pin {
        private VectorInterface pos;
        private boolean showLabel;

        private Pin(VectorInterface pos, boolean showLabel) {
            this.pos = pos;
            this.showLabel = showLabel;
        }

        boolean isShowLabel() {
            return showLabel;
        }

        Vector getPos() {
            return new Vector(pos.getX(), pos.getY());
        }
    }
}
