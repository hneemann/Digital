/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes.custom;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;

import java.util.ArrayList;
import java.util.Iterator;

import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Orientation;
import de.neemann.digital.draw.graphics.Polygon;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.Vector;
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
    public CustomShapeDescription addPin(String name, Vector pos, boolean input) {
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
     * Get the names of all integrated Pins
     * @return list of Pins
     */
    public SVGPseudoPin[] getPinNames() {
        return pins.toArray(new SVGPseudoPin[pins.size()]);
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
    public CustomShapeDescription addLine(Vector p1, Vector p2, Style style) {
        drawables.add(new LineHolder(p1, p2, style));
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
    public CustomShapeDescription addCircle(Vector p1, Vector p2, Style style) {
        drawables.add(new CircleHolder(p1, p2, style));
        return this;
    }

    /**
     * Adds a polygon to the shape
     * @param poly
     *            the polygon to add
     * @param style
     *            style
     * @return this for chained calls
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
     * @param style
     *            style
     * @return this for chained calls
     */
    public CustomShapeDescription addText(Vector p1, Vector p2, String text, Orientation orientation, Style style) {
        drawables.add(new TextHolder(p1, p2, text, orientation, style));
        return this;
    }

    /**
     * Returns the position of the given pin.
     * @param name
     *            the name of the pin
     * @return the position of the pin
     * @throws PinException
     *             thrown if pin is not found
     */
    Vector getPinPos(String name) throws PinException {
        for (SVGPseudoPin p : pins)
            if (p.getLabel().equals(name))
                return p.getPos();
        throw new PinException(Lang.get("err_pin_N_notFound", name));
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
                .addPin("B", new Vector(0, SIZE * 2), true).addPin("Y", new Vector(SIZE * 3, SIZE), false)
                .addCircle(new Vector(0, -SIZE2), new Vector(SIZE * 3, SIZE * 3 - SIZE2), Style.NORMAL)
                .addLine(new Vector(0, -SIZE2), new Vector(SIZE * 3, SIZE * 3 - SIZE2), Style.NORMAL);
    }

    private static final class LineHolder implements Drawable {
        private final Vector p1;
        private final Vector p2;
        private final Style style;

        private LineHolder(Vector p1, Vector p2, Style style) {
            this.p1 = p1;
            this.p2 = p2;
            this.style = style;
        }

        @Override
        public void drawTo(Graphic graphic, Style highLight) {
            graphic.drawLine(p1, p2, style);
        }
    }

    private static final class CircleHolder implements Drawable {
        private final Vector p1;
        private final Vector p2;
        private final Style style;

        private CircleHolder(Vector p1, Vector p2, Style style) {
            this.p1 = p1;
            this.p2 = p2;
            this.style = style;
        }

        @Override
        public void drawTo(Graphic graphic, Style highLight) {
            graphic.drawCircle(p1, p2, style);
        }
    }

    private static final class PolygonHolder implements Drawable {
        private final Polygon poly;
        private final Style style;

        private PolygonHolder(Polygon poly, Style style) {
            this.poly = poly;
            this.style = style;
        }

        @Override
        public void drawTo(Graphic graphic, Style highLight) {
            graphic.drawPolygon(poly, style);
        }
    }

    private static final class TextHolder implements Drawable {
        private final Vector p1;
        private final Vector p2;
        private final String text;
        private final Orientation orientation;
        private final Style style;

        private TextHolder(Vector p1, Vector p2, String text, Orientation orientation, Style style) {
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
}
