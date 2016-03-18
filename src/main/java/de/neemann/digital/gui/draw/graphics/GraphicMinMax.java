package de.neemann.digital.gui.draw.graphics;

/**
 * @author hneemann
 */
public class GraphicMinMax implements Graphic {

    private Vector min;
    private Vector max;

    @Override
    public void drawLine(Vector p1, Vector p2, Style style) {
        check(p1);
        check(p2);
    }

    @Override
    public void drawPolygon(Polygon p, Style style) {
        for (Vector v : p.getPoints())
            check(v);
    }

    @Override
    public void drawCircle(Vector p1, Vector p2, Style style) {
        check(p1);
        check(p2);
    }

    private void check(Vector p) {
        if (min == null || max == null) {
            min = new Vector(p.x, p.y);
            max = new Vector(p.x, p.y);
        } else {
            min = Vector.min(min, p);
            max = Vector.max(max, p);
        }
    }

    @Override
    public void drawText(Vector p1, Vector p2, String text, Orientation orientation) {
        // ignore text!
    }

    public Vector getMin() {
        return min;
    }

    public Vector getMax() {
        return max;
    }
}
