package de.neemann.digital.draw.graphics;

import de.neemann.digital.core.ObservableValue;

import java.awt.*;

/**
 * @author hneemann
 */
public class Style {
    /**
     * used for all lines to draw the shapes itself
     */
    public static final Style NORMAL = new Style(4, false, Color.BLACK);
    /**
     * Used for text which is integral part of the shape.
     * Text which uses this style is always included in sizing!
     * Used for text only elements.
     */
    public static final Style NORMAL_TEXT = new Style(4, false, Color.BLACK);
    /**
     * thin line used for the graphic in the clock or delay shape
     */
    public static final Style THIN = new Style(2, false, Color.BLACK);
    /**
     * Used for wires in editing mode
     */
    public static final Style WIRE = new Style(4, true, Color.BLUE.darker());
    /**
     * Used for low wires in running mode
     */
    public static final Style WIRE_LOW = new Style(4, true, new Color(0, 112, 0));
    /**
     * Used for high wires in running mode
     */
    public static final Style WIRE_HIGH = new Style(4, true, new Color(102, 255, 102));
    /**
     * Used for wires in high Z state
     */
    public static final Style WIRE_HIGHZ = new Style(4, true, Color.GRAY);
    /**
     * used to draw the output dots
     */
    public static final Style WIRE_OUT = new Style(4, true, Color.RED.darker());
    /**
     * filld style used to fill the splitter or the dark LEDs
     */
    public static final Style FILLED = new Style(4, true, Color.BLACK);
    /**
     * Used to draw the grid in the graph
     */
    public static final Style DASH = new Style(1, false, Color.BLACK, new float[]{4, 4});
    /**
     * Used todraw the pin description text
     */
    public static final Style SHAPE_PIN = new Style(4, false, Color.GRAY, 18, null);
    /**
     * highlight color used for the circles to mark an element
     */
    public static final Style HIGHLIGHT = new Style(4, false, Color.CYAN);

    private final int thickness;
    private final boolean filled;
    private final Color color;
    private final int fontsize;
    private final float[] dash;
    private final Stroke stroke;
    private final Font font;

    private Style(int thickness, boolean filled, Color color, float[] dash) {
        this(thickness, filled, color, 24, dash);
    }

    /**
     * Creates a new style
     *
     * @param thickness the line thickness
     * @param filled    true if polygons needs to be filled
     * @param color     the color to use
     */
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

    /**
     * @return the lines thickness
     */
    public int getThickness() {
        return thickness;
    }

    /**
     * @return true if polygons and circles are filled
     */
    public boolean isFilled() {
        return filled;
    }

    /**
     * @return the color
     */
    public Color getColor() {
        return color;
    }

    /**
     * @return the Swing stroke which represents this style
     */
    public Stroke getStroke() {
        return stroke;
    }

    /**
     * @return the font size
     */
    public int getFontSize() {
        return fontsize;
    }

    /**
     * @return the font to use
     */
    public Font getFont() {
        return font;
    }

    /**
     * @return the dash style
     */
    public float[] getDash() {
        return dash;
    }

    /**
     * Returns the wire style depending of the actual value represented by this value
     *
     * @param value the value to represent
     * @return the style
     */
    public static Style getWireStyle(ObservableValue value) {
        if (value == null || value.getBits() > 1) return WIRE;
        if (value.isHighZ()) return WIRE_HIGHZ;

        if (value.getValueIgnoreBurn() == 1) return WIRE_HIGH;
        else return WIRE_LOW;
    }
}
