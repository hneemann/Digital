package de.neemann.digital.draw.graphics;

/**
 * @author hneemann
 */
public class GraphicTransform implements Graphic {

    private final Graphic parent;
    private final Transform transform;

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
        Polygon pp = new Polygon(p.isClosed());
        for (Vector v : p.getPoints())
            pp.add(transform(v));
        parent.drawPolygon(pp, style);
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
}
