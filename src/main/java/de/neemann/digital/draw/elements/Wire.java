package de.neemann.digital.draw.elements;

import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.draw.shapes.Drawable;

import java.util.Collection;

/**
 * A simple wire described by two points
 *
 * @author hneemann
 */
public class Wire implements Drawable, Moveable {
    private static final int MIN_LABEL_LEN = 100;
    /**
     * The first endpoint of the line
     */
    public Vector p1;
    /**
     * The second endpoint of the line
     */
    public Vector p2;
    private transient ObservableValue value;
    private transient boolean p1Dot;
    private transient boolean p2Dot;

    /**
     * Creates anew wire
     *
     * @param p1 one end point
     * @param p2 the other end point
     */
    public Wire(Vector p1, Vector p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    /**
     * Copies a wire
     *
     * @param proto the wire to copy
     */
    public Wire(Wire proto) {
        this.p1 = new Vector(proto.p1);
        this.p2 = new Vector(proto.p2);
        this.p1Dot = proto.p1Dot;
        this.p2Dot = proto.p2Dot;
    }

    @Override
    public void drawTo(Graphic graphic, boolean highLight) {
        Style style = Style.HIGHLIGHT;
        if (!highLight)
            style = Style.getWireStyle(value);

        graphic.drawLine(p1, p2, style);

        if (value != null && p1.y == p2.y && Math.abs(p1.x - p2.x) > MIN_LABEL_LEN && value.getBits() > 1) {
            Vector pos = p1.add(p2).div(2).add(0, -3);
            graphic.drawText(pos, pos.add(1, 0), value.getValueString(), de.neemann.digital.draw.graphics.Orientation.CENTERBOTTOM, Style.SHAPE_PIN);
        }

        if (p1Dot || p2Dot) {
            Vector r = new Vector(style.getThickness(), style.getThickness());
            if (p1Dot)
                graphic.drawCircle(p1.sub(r), p1.add(r), style);
            if (p2Dot)
                graphic.drawCircle(p2.sub(r), p2.add(r), style);
        }
    }

    @Override
    public void move(Vector delta) {
        p1 = p1.add(delta);
        p2 = p2.add(delta);
    }

    @Override
    public Vector getPos() {
        return p1;
    }

    /**
     * Setter for point 2.
     * Is used to move the line with the mouse
     *
     * @param p2 the new point
     */
    public void setP2(Vector p2) {
        this.p2 = p2;
    }

    /**
     * Checks if the given point is on the wire.
     * Only works on vertical and horizontal lines
     *
     * @param v the given point
     * @return true if the point matches the wire
     */
    public boolean contains(Vector v) {
        if (p1.x == p2.x && p1.x == v.x)
            return (p1.y < v.y && v.y < p2.y) || (p2.y < v.y && v.y < p1.y);
        else if (p1.y == p2.y && p1.y == v.y)
            return (p1.x < v.x && v.x < p2.x) || (p2.x < v.x && v.x < p1.x);
        else
            return false;
    }

    /**
     * Test if the given wire is matched by the given position.
     * Returns true if distance to wire is smaller then the given radius.
     *
     * @param v      the position
     * @param radius the matching radius
     * @return true if matching
     */
    public boolean contains(Vector v, int radius) {
        if (p1.x == p2.x && Math.abs(p1.x - v.x) <= radius)
            return (p1.y < v.y && v.y < p2.y) || (p2.y < v.y && v.y < p1.y);
        else if (p1.y == p2.y && Math.abs(p1.y - v.y) < radius)
            return (p1.x < v.x && v.x < p2.x) || (p2.x < v.x && v.x < p1.x);
        else
            return false; // ToDo: should also work for diagonal wires
    }

    /**
     * @return the orientation of the wire
     */
    public Orientation getOrientation() {
        if (p1.x == p2.x)
            return Orientation.vertical;
        if (p1.y == p2.y)
            return Orientation.horzontal;
        return Orientation.diagonal;
    }

    /**
     * Returns true if the given wire is included in the given collection.
     * To compare the wires it calls equalsContent.
     *
     * @param col the collection of wires
     * @return true if wire is included
     * @see Wire#equalsContent(Wire)
     */
    public boolean isIncludedIn(Collection<Wire> col) {
        for (Wire w : col)
            if (equalsContent(w))
                return true;
        return false;
    }

    /**
     * Returns true if wires are equal.
     * It is not possible to overwrite Object.equals() because some algorithms
     * (eg. highlighting) are depending on an object based equals!
     *
     * @param wire the other wire
     * @return true if both wires are equal
     */
    public boolean equalsContent(Wire wire) {
        if (this == wire) return true;
        if (wire == null) return false;

        if (!p1.equals(wire.p1)) return false;
        return p2.equals(wire.p2);
    }

    @Override
    public String toString() {
        return "Wire{"
                + "p1=" + p1
                + ", p2=" + p2
                + '}';
    }

    /**
     * Sets the {@link ObservableValue} which is represented by this wire
     *
     * @param value
     */
    public void setValue(ObservableValue value) {
        this.value = value;
    }

    /**
     * @return returns the value which is represented by this wire
     */
    public ObservableValue getValue() {
        return value;
    }

    /**
     * Turns of the visualisation of the wire dots
     */
    public void noDot() {
        p1Dot = false;
        p2Dot = false;
    }

    /**
     * Turns on the wire dot for the given position
     *
     * @param p the position
     */
    public void setDot(Vector p) {
        if (p.equals(p1)) p1Dot = true;
        if (p.equals(p2)) p2Dot = true;
    }

    /**
     * @return a moveable representing point one
     */
    public Moveable getMovableP1() {
        return new Moveable() {
            @Override
            public void move(Vector delta) {
                p1 = p1.add(delta);
            }

            @Override
            public Vector getPos() {
                return p1;
            }
        };
    }

    /**
     * @return a moveable representing point two
     */
    public Moveable getMovableP2() {
        return new Moveable() {
            @Override
            public void move(Vector delta) {
                p2 = p2.add(delta);
            }

            @Override
            public Vector getPos() {
                return p2;
            }
        };
    }

    enum Orientation {horzontal, vertical, diagonal}
}
