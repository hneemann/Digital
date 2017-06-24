package de.neemann.digital.draw.graphics;

/**
 * A instance that performs a transformation on the drawing and then draws it on a given delegate.
 *
 * @author hneemann
 */
public class GraphicTransform implements Graphic {

    private final Graphic parent;
    private final Transform transform;

    /**
     * Creates a new instace
     *
     * @param parent    the delegate to be used to berform the drawing
     * @param transform the transformation
     */
    public GraphicTransform(Graphic parent, Transform transform) {
        this.parent = parent;
        this.transform = transform;
    }

    @Override
    public void drawLine(Vector p1, Vector p2, Style style) {
        parent.drawLine(transform(p1), transform(p2), style);
    }

    @Override
    public void drawPolygon(Polygon p, Style style) {
        parent.drawPolygon(p.transform(transform), style);
    }

    @Override
    public void drawCircle(Vector p1, Vector p2, Style style) {
        parent.drawCircle(transform(p1), transform(p2), style);
    }

    @Override
    public void drawText(Vector p1, Vector p2, String text, Orientation orientation, Style style) {
        parent.drawText(transform(p1), transform(p2), text, orientation, style);
    }

    private Vector transform(Vector v) {
        return transform.transform(v);
    }

    @Override
    public boolean isFlagSet(String name) {
        return parent.isFlagSet(name);
    }
}
