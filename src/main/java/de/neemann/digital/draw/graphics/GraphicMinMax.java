package de.neemann.digital.draw.graphics;

import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;

import static de.neemann.digital.core.element.ElementAttributes.cleanLabel;

/**
 * This class is used to determine the size of shapes or the whole circuit.
 * You can draw the items to an instance of this class and then obtain the size
 * by the getters getMin() and getMax().
 *
 * @author hneemann
 */
public class GraphicMinMax implements Graphic {

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

    /**
     * Checks the given point a makes the bounding box larger if necessary
     *
     * @param p the point to check
     */
    public void check(Vector p) {
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
        if (includeText || style.mattersAlwaysForSize())
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
        if (text != null && text.length() > 0) {
            Vector delta = p2.sub(p1).norm128();
            Vector height = new Vector(delta.y, -delta.x).mul(style.getFontSize()).div(128);

            int textWidth = getTextWidth(text, style);
            Vector width = delta.mul(textWidth).div(128);

            Vector p = p1;
            if (orientation.getX() != 0) {
                p = p.sub(width.mul(orientation.getX()).div(2));
            }

            if (orientation.getY() != 0) {
                p = p.sub(height.mul(orientation.getY()).div(2));
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
        text = cleanLabel(text);
        Rectangle2D sb = style.getFont().getStringBounds(text, new FontRenderContext(null, true, false));
        return (int) sb.getWidth();
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
    public boolean isFlagSet(String name) {
        if (parent == null)
            return false;
        else
            return parent.isFlagSet(name);
    }
}
