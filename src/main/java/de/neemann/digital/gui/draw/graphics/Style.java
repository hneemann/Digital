package de.neemann.digital.gui.draw.graphics;

import java.awt.*;

/**
 * @author hneemann
 */
public class Style {
    public static final Style NORMAL = new Style(2, false, Color.BLACK);
    public static final Style WIRE = new Style(3, true, new Color(0, 112, 0));
    public static final Style WIRE_HIGH = new Style(3, true, new Color(102, 255, 102));
    public static final Style FILLED = new Style(2, true, Color.BLACK);
    public static final Style THIN = new Style(1, false, Color.BLACK);

    private final int thickness;
    private final boolean filled;
    private Color color;

    private Style(int thickness, boolean filled, Color color) {
        this.thickness = thickness;
        this.filled = filled;
        this.color = color;
    }

    public int getThickness() {
        return thickness;
    }

    public boolean isFilled() {
        return filled;
    }

    public Color getColor() {
        return color;
    }
}
