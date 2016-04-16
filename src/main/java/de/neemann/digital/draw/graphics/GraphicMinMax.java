package de.neemann.digital.draw.graphics;

/**
 * This class is used to determine the size of shapes or the whole circuit.
 * You can draw the items to a instance of this class an then obtain the size
 * by the getters getMin() and getMax().
 *
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
        for (Vector v : p)
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
    public void drawText(Vector p1, Vector p2, String text, Orientation orientation, Style style) {
        approxTextSize(this, p1, p2, text, orientation, style);
    }

    /**
     * Approximation of text size
     *
     * @param gr          the Graphic instance to use
     * @param p1          point to draw the text
     * @param p2          at the left of p1, is used to determine the correct orientation of the text after transforming coordinates
     * @param text        the text
     * @param orientation the texts orientation
     * @param style       the text style
     */
    public static void approxTextSize(Graphic gr, Vector p1, Vector p2, String text, Orientation orientation, Style style) {
        Vector delta = p2.sub(p1).norm128();
        Vector height = new Vector(delta.y, -delta.x).mul(style.getFontSize()).div(128);
        Vector width = delta.mul(text.length() * style.getFontSize()).div(190);


        if (p1.y == p2.y) {   // 0 and 180 deg
            if (p1.x > p2.x)   // 180
                orientation = orientation.rot(2);
        } else {
            if (p1.y < p2.y) // 270
                orientation = orientation.rot(2);
            else            // 90
                orientation = orientation.rot(0);
        }

        Vector p = p1;
        if (orientation.getX() != 0) {
            p = p.sub(width.mul(orientation.getX()).div(2));
        }


        if (orientation.getY() != 0) {
            p = p.sub(height.mul(orientation.getY()).div(2));
        }
        p.sub(height.div(3));

        gr.drawPolygon(new Polygon(true)
                .add(p)
                .add(p.add(width))
                .add(p.add(width).add(height))
                .add(p.add(height)), Style.THIN);
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
}
