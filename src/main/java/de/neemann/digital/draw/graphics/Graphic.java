package de.neemann.digital.draw.graphics;

/**
 * @author hneemann
 */
public interface Graphic {

    /**
     * Draws a line
     *
     * @param p1    first point
     * @param p2    second point
     * @param style the line style
     */
    void drawLine(Vector p1, Vector p2, Style style);

    /**
     * Draws a polygon
     *
     * @param p     the polygon
     * @param style the style
     */
    void drawPolygon(Polygon p, Style style);

    /**
     * Draws a circle
     *
     * @param p1    upper left corner of outer rectangle containing the circle
     * @param p2    lower right corner of outer rectangle containing the circle
     * @param style the style
     */
    void drawCircle(Vector p1, Vector p2, Style style);

    /**
     * Draws text
     *
     * @param p1          point to draw the text
     * @param p2          at the left of p1, is used to determine the correct orientation of the text after transforming coordinates
     * @param text        the text
     * @param orientation the texts orientation
     * @param style       the text style
     */
    void drawText(Vector p1, Vector p2, String text, Orientation orientation, Style style);

    /**
     * opens a new group, used to create SVG grouping
     */
    default void openGroup() {
    }

    /**
     * closes a group, used to create SVG grouping
     */
    default void closeGroup() {
    }

}
