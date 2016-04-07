package de.neemann.digital.draw.graphics;

import de.neemann.digital.core.ObservableValue;

import java.awt.*;

/**
 * @author hneemann
 */
public class Style {
    public static final Style NORMAL = new Style(4, false, Color.BLACK);
    public static final Style THIN = new Style(2, false, Color.BLACK);
    public static final Style WIRE = new Style(4, true, Color.BLUE.darker());
    public static final Style WIRE_LOW = new Style(4, true, new Color(0, 112, 0));
    public static final Style WIRE_HIGH = new Style(4, true, new Color(102, 255, 102));
    public static final Style WIRE_HIGHZ = new Style(4, true, Color.GRAY);
    public static final Style WIRE_OUT = new Style(4, true, Color.RED.darker());
    public static final Style FILLED = new Style(4, true, Color.BLACK);
    public static final Style DASH = new Style(1, false, Color.BLACK, new float[]{4, 4});
    public static final Style SHAPE_PIN = new Style(4, false, Color.GRAY, 18, null);
    public static final Style HIGHLIGHT = new Style(4, false, Color.CYAN);

    private final int thickness;
    private final boolean filled;
    private final Color color;
    private final int fontsize;
    private final float[] dash;
    private final Stroke stroke;
    private final Font font;

    public Style(int thickness, boolean filled, Color color, float[] dash) {
        this(thickness, filled, color, 24, dash);
    }

    public Style(int thickness, boolean filled, Color color) {
        this(thickness, filled, color, 24, null);
    }

    private Style(int thickness, boolean filled, Color color, int fontsize, float[] dash) {
        this.thickness = thickness;
        this.filled = filled;
        this.color = color;
        this.fontsize = fontsize;
        this.dash = dash;
        stroke = new BasicStroke(thickness, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10f, dash, 0f);

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

    public float[] getDash() {
        return dash;
    }

    public static Style getWireStyle(ObservableValue value) {
        if (value == null || value.getBits() > 1) return WIRE;
        if (value.isHighZIgnoreBurn()) return WIRE_HIGHZ;

        if (value.getValueIgnoreBurn() == 1) return WIRE_HIGH;
        else return WIRE_LOW;
    }
}
