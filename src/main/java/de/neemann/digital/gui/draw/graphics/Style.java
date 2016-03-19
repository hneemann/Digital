package de.neemann.digital.gui.draw.graphics;

import java.awt.*;

/**
 * @author hneemann
 */
public class Style {
    public static final Style NORMAL = new Style(2, false, Color.BLACK);
    public static final Style WIRE_LOW = new Style(3, true, new Color(0, 112, 0));
    public static final Style WIRE_HIGH = new Style(3, true, new Color(102, 255, 102));
    public static final Style FILLED = new Style(2, true, Color.BLACK);
    public static final Style THIN = new Style(1, false, Color.BLACK);
    public static final Style DASH = new Style(1, false, Color.BLACK);
    public static final Style SHAPE_PIN = new Style(2, false, Color.GRAY, 9);

    private final int thickness;
    private final boolean filled;
    private final Color color;
    private final int fontsize;
    private final Stroke stroke;
    private final Font font;

    public Style(int thickness, boolean filled, Color color) {
        this(thickness, filled, color, 12);
    }

    private Style(int thickness, boolean filled, Color color, int fontsize) {
        this.thickness = thickness;
        this.filled = filled;
        this.color = color;
        this.fontsize = fontsize;
        stroke = new BasicStroke(thickness);

        font = new Font("Arial", Font.PLAIN, fontsize);
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

    public Stroke getStroke() {
        return stroke;
    }

    public int getFontSize() {
        return fontsize;
    }

    public Font getFont() {
        return font;
    }

}
