package de.neemann.digital.draw.graphics;

import de.neemann.digital.core.Value;

import java.awt.*;

/**
 * @author hneemann
 */
public final class Style {
    /**
     * maximal line thickness
     */
    public static final int MAXLINETHICK = 4;
    /**
     * thickness of thin lines
     */
    public static final int LINETHIN = MAXLINETHICK / 2;

    private static final int WIRETHICK = MAXLINETHICK;
    private static final int LINETHICK = MAXLINETHICK;
    private static final int LINEDASH = 1;

    /**
     * used for all lines to draw the shapes itself
     */
    public static final Style NORMAL = new Style(LINETHICK, false, Color.BLACK);
    /**
     * used for all lines to draw the failed state
     */
    public static final Style FAILED = new Style(LINETHICK, false, Color.RED);
    /**
     * used for all lines to draw the passed state
     */
    public static final Style PASS = new Style(LINETHICK, false, Color.GREEN);
    /**
     * Used for text which is integral part of the shape.
     * Text which uses this style is always included in sizing!
     * Used for text only elements.
     */
    public static final Style NORMAL_TEXT = new Style(LINETHICK, false, Color.BLACK, 24, null, true);
    /**
     * thin line used for the graphic in the clock or delay shape
     */
    public static final Style THIN = new Style(LINETHIN, false, Color.BLACK);
    /**
     * thin filled
     */
    public static final Style THIN_FILLED = new Style(LINETHIN, true, Color.BLACK);
    /**
     * thick line used for the ground line
     */
    public static final Style THICK = new Style(LINETHICK + LINETHIN, false, Color.BLACK);
    /**
     * Used for wires in editing mode
     */
    public static final Style WIRE = new Style(WIRETHICK, true, Color.BLUE.darker());
    /**
     * Used for low wires in running mode
     */
    public static final Style WIRE_LOW = new Style(WIRETHICK, true, new Color(0, 142, 0));
    /**
     * Used for high wires in running mode
     */
    public static final Style WIRE_HIGH = new Style(WIRETHICK, true, new Color(102, 255, 102));
    /**
     * Used for wires in high Z state
     */
    public static final Style WIRE_HIGHZ = new Style(WIRETHICK, true, Color.GRAY);
    /**
     * used to draw the output dots
     */
    public static final Style WIRE_OUT = new Style(LINETHICK, true, Color.RED.darker());

    /**
     * used to draw the bus wires
     */
    public static final Style WIRE_BUS = WIRE;

    /**
     * Filled style used to fill the splitter or the dark LEDs
     */
    public static final Style FILLED = new Style(LINETHICK, true, Color.BLACK);
    /**
     * Used to draw the grid in the graph
     */
    public static final Style DASH = new Style(LINEDASH, false, Color.BLACK, new float[]{4, 4});
    /**
     * Used to draw the pin description text
     */
    public static final Style SHAPE_PIN = new Style(LINETHIN, false, Color.GRAY, 18, null, false);
    /**
     * Used to draw the pin description text for splitters
     */
    public static final Style SHAPE_SPLITTER = new Style(LINETHIN, false, Color.GRAY, 12, null, false);
    /**
     * Used to draw the pin description text
     */
    public static final Style WIRE_VALUE = new Style(LINETHICK, false, new Color(50, 162, 50), 12, null, false);
    /**
     * Used to draw the wire bit number
     */
    public static final Style WIRE_BITS = new Style(LINETHIN, false, WIRE.color, 12, null, false);
    /**
     * highlight color used for the circles to mark an element
     */
    public static final Style HIGHLIGHT = new Style(WIRETHICK, false, Color.CYAN);

    /**
     * error color used for the circles to mark an element
     */
    public static final Style ERROR = new Style(WIRETHICK, false, Color.RED);

    private final int thickness;
    private final boolean filled;
    private final Color color;
    private final int fontSize;
    private final float[] dash;
    private final Stroke stroke;
    private final Font font;
    private final boolean mattersForSize;

    private Style(int thickness, boolean filled, Color color, float[] dash) {
        this(thickness, filled, color, 24, dash, false);
    }

    /**
     * Creates a new style
     *
     * @param thickness the line thickness
     * @param filled    true if polygons needs to be filled
     * @param color     the color to use
     */
    private Style(int thickness, boolean filled, Color color) {
        this(thickness, filled, color, 24, null, false);
    }

    /**
     * Creates a new style
     *
     * @param thickness      the line thickness
     * @param filled         true if polygons needs to be filled
     * @param color          the color to use
     * @param fontSize       font size
     * @param dash           dash intervals, null is allowed for a solid line
     * @param mattersForSize always include in shape size measurement
     */
    private Style(int thickness, boolean filled, Color color, int fontSize, float[] dash, boolean mattersForSize) {
        this.thickness = thickness;
        this.filled = filled;
        this.color = color;
        this.fontSize = fontSize;
        this.dash = dash;
        this.mattersForSize = mattersForSize;
        stroke = new BasicStroke(thickness, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10f, dash, 0f);

        font = new Font("Arial", Font.PLAIN, fontSize);
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
    boolean isFilled() {
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
        return fontSize;
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
    float[] getDash() {
        return dash;
    }

    /**
     * Returns the wire style depending on the given value
     *
     * @param value the value to represent
     * @return the style
     */
    public static Style getWireStyle(Value value) {
        if (value == null) return WIRE;
        if (value.isHighZ()) return WIRE_HIGHZ;
        if (value.getBits() > 1) return WIRE_BUS;

        if (value.getValue() == 1) return WIRE_HIGH;
        else return WIRE_LOW;
    }

    /**
     * If this flag is set, the text is always to include in size estimation.
     *
     * @return the mattersForSize flag
     */
    boolean mattersAlwaysForSize() {
        return mattersForSize;
    }

    /**
     * Creates a new style, based on this style.
     *
     * @param fontSize       the new font size
     * @param mattersForSize the mattersForSize flag
     * @return Style the derived style with the given font size and mattersForSize flag.
     */
    public Style deriveFontStyle(int fontSize, boolean mattersForSize) {
        return new Style(thickness, filled, color, fontSize, dash, mattersForSize);
    }

    /**
     * Creates a new style, based on this style.
     *
     * @param thickness the line thickness
     * @param filled    filled flag for polygons
     * @param color     the color
     * @return the new style
     */
    public Style deriveStyle(int thickness, boolean filled, Color color) {
        return new Style(thickness, filled, color, fontSize, dash, mattersForSize);
    }

    /**
     * Creates a new style suited for filling polygons, based on this style.
     *
     * @param color the fill color
     * @return the nes style
     */
    public Style deriveFillStyle(Color color) {
        return new Style(1, true, color, fontSize, dash, mattersForSize);
    }

}
