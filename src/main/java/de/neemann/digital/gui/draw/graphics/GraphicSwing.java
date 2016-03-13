package de.neemann.digital.gui.draw.graphics;

import java.awt.*;

/**
 * @author hneemann
 */
public class GraphicSwing implements Graphic {

    private final Graphics2D gr;

    public GraphicSwing(Graphics2D gr) {
        this.gr = gr;
    }

    @Override
    public void drawLine(Vector p1, Vector p2, Style style) {
        gr.drawLine(p1.x, p1.y, p2.x, p2.y);
    }

    @Override
    public void drawPolygon(Polygon p, Style style) {
        java.awt.Polygon poly = new java.awt.Polygon();
        for (Vector v : p.getPoints())
            poly.addPoint(v.x, v.y);
        gr.draw(poly);
    }

    @Override
    public void drawCircle(Vector p1, Vector p2, Style style) {
        Vector p = Vector.min(p1, p2);
        Vector w = Vector.width(p1, p2);
        gr.drawOval(p.x, p.y, w.x, w.y);
    }

    @Override
    public void drawText(Vector p1, Vector p2, String text) {
        gr.drawString(text, p1.x, p1.y);
    }
}
