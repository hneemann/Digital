package de.neemann.digital.gui.draw.graphics;

/**
 * @author hneemann
 */
public interface Graphic {

    void drawLine(Vector p1, Vector p2, Style style);

    void drawPolygon(Polygon p, Style style);

    void drawCircle(Vector p1, Vector p2, Style style);

    void drawText(Vector p1, Vector p2, String text, Orientation orientation, Style style);
}
