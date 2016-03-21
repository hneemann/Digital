package de.neemann.digital.gui.draw.elements;

import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.gui.draw.graphics.Graphic;
import de.neemann.digital.gui.draw.graphics.Style;
import de.neemann.digital.gui.draw.graphics.Vector;
import de.neemann.digital.gui.draw.shapes.Drawable;

/**
 * @author hneemann
 */
public class Wire implements Drawable, Moveable {
    public Vector p1;
    public Vector p2;
    private transient ObservableValue value;
    private transient boolean highLight = false;
    private transient boolean p1Dot;
    private transient boolean p2Dot;

    public Wire(Vector p1, Vector p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    public Wire(Wire proto) {
        this.p1 = new Vector(proto.p1);
        this.p2 = new Vector(proto.p2);
        this.p1Dot = proto.p1Dot;
        this.p2Dot = proto.p2Dot;
    }

    @Override
    public void drawTo(Graphic graphic) {
        Style style = Style.getWireStyle(value);
        if (highLight)
            style = Style.HIGHLIGHT;

        graphic.drawLine(p1, p2, style);

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

    public void setP2(Vector p2) {
        this.p2 = p2;
    }

    public boolean contains(Vector v) {
        if (p1.x == p2.x && p1.x == v.x)
            return (p1.y < v.y && v.y < p2.y) || (p2.y < v.y && v.y < p1.y);
        else if (p1.y == p2.y && p1.y == v.y)
            return (p1.x < v.x && v.x < p2.x) || (p2.x < v.x && v.x < p1.x);
        else
            return false;
    }

    public Orientation getOrientation() {
        if (p1.x == p2.x)
            return Orientation.vertical;
        if (p1.y == p2.y)
            return Orientation.horzontal;
        return Orientation.diagonal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Wire wire = (Wire) o;

        if (!p1.equals(wire.p1)) return false;
        return p2.equals(wire.p2);

    }

    @Override
    public int hashCode() {
        int result = p1.hashCode();
        result = 31 * result + p2.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Wire{" +
                "p1=" + p1 +
                ", p2=" + p2 +
                '}';
    }

    public void setValue(ObservableValue value) {
        this.value = value;
    }

    public void setHighLight(boolean highLight) {
        this.highLight = highLight;
    }

    public void noDot() {
        p1Dot = false;
        p2Dot = false;
    }

    public void setDot(Vector p) {
        if (p.equals(p1)) p1Dot = true;
        if (p.equals(p2)) p2Dot = true;
    }

    enum Orientation {horzontal, vertical, diagonal}
}
