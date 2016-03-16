package de.neemann.digital.gui.draw.graphics;

import java.awt.*;

/**
 * @author hneemann
 */
public class GraphicSwing implements Graphic {

    private final Graphics2D gr;
    private Style lastStyle;

    public GraphicSwing(Graphics2D gr) {
        this.gr = gr;
    }

    @Override
    public void drawLine(Vector p1, Vector p2, Style style) {
        applyStyle(style);
        gr.drawLine(p1.x, p1.y, p2.x, p2.y);
    }

    @Override
    public void drawPolygon(Polygon p, Style style) {
        applyStyle(style);
        java.awt.Polygon poly = new java.awt.Polygon();
        for (Vector v : p.getPoints())
            poly.addPoint(v.x, v.y);
        gr.draw(poly);
    }

    @Override
    public void drawCircle(Vector p1, Vector p2, Style style) {
        applyStyle(style);
        Vector p = Vector.min(p1, p2);
        Vector w = Vector.width(p1, p2);
        if (style.isFilled())
            gr.fillOval(p.x - 1, p.y - 1, w.x + 2, w.y + 2);
        else
            gr.drawOval(p.x, p.y, w.x, w.y);
    }

    private void applyStyle(Style style) {
        if (style != lastStyle) {
            gr.setStroke(new BasicStroke(style.getThickness()));
            gr.setColor(style.getColor());
            lastStyle = style;
        }
    }

    @Override
    public void drawText(Vector p1, Vector p2, String text) {
        gr.drawString(text, p1.x, p1.y);
    }
}
