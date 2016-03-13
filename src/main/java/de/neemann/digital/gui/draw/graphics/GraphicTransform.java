package de.neemann.digital.gui.draw.graphics;

/**
 * @author hneemann
 */
public class GraphicTransform implements Graphic {

    private final Graphic parent;
    private final Vector pos;
    private final int rotate;

    public GraphicTransform(Graphic parent, Vector pos, int rotate) {
        this.parent = parent;
        this.pos = pos;
        this.rotate = rotate;
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
    public void drawText(Vector p1, Vector p2, String text) {
        parent.drawText(transform(p1), transform(p2), text);
    }

    public Vector transform(Vector v) {
        return v.add(pos);
    }
}
