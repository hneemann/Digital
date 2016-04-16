package de.neemann.digital.draw.graphics;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;

/**
 * Used to draw on a {@link Graphics2D} instance.
 *
 * @author hneemann
 */
public class GraphicSwing implements Graphic {

    private final Graphics2D gr;
    private Style lastStyle;

    /**
     * Creates a new instance
     *
     * @param gr the {@link Graphics2D} instave to use.
     */
    public GraphicSwing(Graphics2D gr) {
        this.gr = gr;
        gr.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    @Override
    public void drawLine(Vector p1, Vector p2, Style style) {
        if (style != Style.INVISIBLE) {
            applyStyle(style);
            gr.drawLine(p1.x, p1.y, p2.x, p2.y);
        }
    }

    @Override
    public void drawPolygon(Polygon p, Style style) {
        if (style != Style.INVISIBLE) {
            applyStyle(style);
            Path2D path = new GeneralPath();
            boolean first = true;
            for (Vector v : p)
                if (first) {
                    first = false;
                    path.moveTo(v.x, v.y);
                } else
                    path.lineTo(v.x, v.y);

            if (p.isClosed())
                path.closePath();

            if (style.isFilled() && p.isClosed())
                gr.fill(path);
            gr.draw(path);
        }
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
            gr.setStroke(style.getStroke());
            gr.setColor(style.getColor());
            gr.setFont(style.getFont());
            lastStyle = style;
        }
    }

    @Override
    public void drawText(Vector p1, Vector p2, String text, Orientation orientation, Style style) {
        if (text == null || text.length() == 0) return;
        if (text.length() >= 3) {
            if ((text.charAt(0) == '$') && (text.charAt(text.length() - 1) == '$'))
                text = text.substring(1, text.length() - 1);
        }

        boolean rotateText = false;
        if (p1.y == p2.y) {   // 0 and 180 deg
            if (p1.x > p2.x)   // 180
                orientation = orientation.rot(2);
        } else {
            if (p1.y < p2.y) // 270
                orientation = orientation.rot(2);
            else            // 90
                orientation = orientation.rot(0);
            rotateText = true;
        }

        AffineTransform old = null;
        if (rotateText) {
            old = gr.getTransform();
            gr.translate(p1.x, p1.y);
            gr.rotate(-Math.PI / 2);
            gr.translate(-p1.x, -p1.y);
        }


        applyStyle(style);
        int xoff = 0;
        if (orientation.getX() != 0) {
            int width = gr.getFontMetrics().stringWidth(text);
            xoff -= width * orientation.getX() / 2;
        }

        int yoff = 0;
        if (orientation.getY() != 0) {
            int height = gr.getFontMetrics().getHeight();
            yoff += height * orientation.getY() / 3;
        }

        gr.drawString(text, p1.x + xoff, p1.y + yoff);

        if (rotateText)
            gr.setTransform(old);
    }
}
