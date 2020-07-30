/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics;

import de.neemann.digital.core.Value;

import java.awt.*;

/**
 * Defines the styles (color, line thickness, font size and style) which are used to draw the circuit.
 */
public final class Style {
    /**
     * maximal line thickness
     */
    public static final int MAXLINETHICK = 4;
    /**
     * thickness of thin lines
     */
    private static final int LINETHIN = MAXLINETHICK / 2;

    private static final int WIRETHICK = MAXLINETHICK;
    private static final int LINETHICK = MAXLINETHICK;
    private static final int LINEDASH = 1;

    /**
     * used for all lines to draw the shapes itself
     */
    public static final Style NORMAL = new Builder().build();
    /**
     * used for all disabled elements
     */
    public static final Style DISABLED = new Builder().setColor(ColorKey.DISABLED).build();
    /**
     * used for input and output labels
     */
    public static final Style INOUT = new Builder(NORMAL).setFontStyle(Font.ITALIC).build();
    /**
     * used to draw the failed state lines in the measurement graph
     */
    public static final Style FAILED = new Builder(NORMAL).setColor(ColorKey.ERROR).build();
    /**
     * used to draw the passed state lines in the measurement graph
     */
    public static final Style PASS = new Builder(NORMAL).setColor(ColorKey.PASSED).build();
    /**
     * Used for text which is integral part of the shape.
     * Text which uses this style is always included in sizing!
     * Used for text only elements.
     */
    public static final Style NORMAL_TEXT = new Builder(NORMAL).setMattersForSize(true).build();
    /**
     * thin line used for the graphic in the clock or delay shape
     */
    public static final Style THIN = new Builder(NORMAL).setThickness(LINETHIN).build();
    /**
     * thin filled
     */
    public static final Style THIN_FILLED = new Builder(NORMAL).setThickness(LINETHIN).setFilled(true).build();
    /**
     * thick line used for the ground line
     */
    public static final Style THICK = new Builder(NORMAL).setThickness(LINETHICK + LINETHIN).build();
    /**
     * Used for wires in editing mode
     */
    public static final Style WIRE = new Builder()
            .setThickness(WIRETHICK)
            .setFilled(true)
            .setColor(ColorKey.WIRE)
            .setEndCap(BasicStroke.CAP_ROUND)
            .build();
    /**
     * Used for low wires in running mode
     */
    public static final Style WIRE_LOW = new Builder(WIRE).setColor(ColorKey.WIRE_LOW).build();
    /**
     * Used for high wires in running mode
     */
    public static final Style WIRE_HIGH = new Builder(WIRE).setColor(ColorKey.WIRE_HIGH).build();
    /**
     * Used for wires in high Z state
     */
    public static final Style WIRE_HIGHZ = new Builder(WIRE).setColor(ColorKey.WIRE_Z).build();
    /**
     * used to draw the output dots
     */
    public static final Style WIRE_OUT = new Builder(WIRE).setColor(ColorKey.WIRE_OUT).build();

    /**
     * Filled style used to fill the splitter or the dark LEDs
     */
    public static final Style FILLED = new Builder().setFilled(true).build();
    /**
     * Used to draw the grid in the graph
     */
    public static final Style DASH = new Builder()
            .setThickness(LINEDASH)
            .setDash(new float[]{4, 4})
            .build();
    /**
     * Used to draw the pin description text
     */
    public static final Style SHAPE_PIN = new Builder()
            .setThickness(LINETHIN)
            .setColor(ColorKey.PINS)
            .setFontSize(18)
            .build();
    /**
     * Used to draw the pin description text for splitters
     */
    public static final Style SHAPE_SPLITTER = new Builder(SHAPE_PIN).setFontSize(12).build();
    /**
     * Used to draw the pin description text
     */
    public static final Style WIRE_VALUE = new Builder(SHAPE_SPLITTER)
            .setColor(ColorKey.WIRE_VALUE)
            .build();
    /**
     * Used to draw the wire bit number
     */
    public static final Style WIRE_BITS = new Builder(SHAPE_SPLITTER)
            .setColor(ColorKey.WIRE)
            .build();
    /**
     * highlight color used for the circles to mark an element
     */
    public static final Style HIGHLIGHT = new Builder(NORMAL)
            .setColor(ColorKey.HIGHLIGHT)
            .setEndCap(BasicStroke.CAP_ROUND)
            .build();

    /**
     * error color used for the circles to mark an element
     */
    public static final Style ERROR = new Builder(NORMAL)
            .setColor(ColorKey.ERROR)
            .setEndCap(BasicStroke.CAP_ROUND)
            .build();

    private final int thickness;
    private final boolean filled;
    private final Color color;
    private final ColorKey colorKey;
    private final int fontSize;
    private final float[] dash;
    private final BasicStroke stroke;
    private final Font font;
    private final boolean mattersForSize;
    private final int fontStyle;

    /**
     * Creates a new style
     *
     * @param builder the builder
     */
    private Style(Builder builder) {
        this.thickness = builder.thickness;
        this.filled = builder.filled;
        this.colorKey = builder.colorKey;
        this.color = builder.color;
        this.fontSize = builder.fontSize;
        this.fontStyle = builder.fontStyle;
        this.dash = builder.dash;
        this.mattersForSize = builder.mattersForSize;

        stroke = new BasicStroke(thickness, builder.endCap, BasicStroke.JOIN_MITER, 10f, dash, 0f);
        font = new Font(null, fontStyle, fontSize);
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
        if (colorKey != null)
            return ColorScheme.getSelected().getColor(colorKey);
        else
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
     * @return the font style
     */
    public int getFontStyle() {
        return fontStyle;
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
        if (value == null || value.getBits() > 1) return WIRE;

        if (value.isHighZ()) return WIRE_HIGHZ;
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
        return new Builder(this)
                .setFontSize(fontSize)
                .setMattersForSize(mattersForSize)
                .build();
    }

    /**
     * Creates a new style, based on this style.
     *
     * @param color the new color
     * @return Style the derived style with the given color set.
     */
    public Style deriveColor(Color color) {
        return new Builder(this)
                .setColor(color)
                .build();
    }

    /**
     * Creates a new style, based on this style.
     *
     * @param colorKey the new color
     * @return Style the derived style with the given color set.
     */
    public Style deriveColor(ColorKey colorKey) {
        return new Builder(this)
                .setColor(colorKey)
                .build();
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
        return new Builder(this)
                .setThickness(thickness)
                .setFilled(filled)
                .setColor(color)
                .build();
    }

    /**
     * Creates a new style suited for filling polygons, based on this style.
     *
     * @param color the fill color
     * @return the nes style
     */
    public Style deriveFillStyle(Color color) {
        return new Builder(this)
                .setThickness(0)
                .setFilled(true)
                .setColor(color)
                .build();
    }

    /**
     * Creates a new style suited for filling polygons, based on this style.
     *
     * @param colorKey the fill color key
     * @return the nes style
     */
    public Style deriveFillStyle(ColorKey colorKey) {
        return new Builder(this)
                .setThickness(0)
                .setFilled(true)
                .setColor(colorKey)
                .build();
    }

    private static final class Builder {
        private int thickness = LINETHICK;
        private boolean filled = false;
        private ColorKey colorKey = ColorKey.MAIN;
        private Color color;
        private int fontSize = 24;
        private float[] dash = null;
        private boolean mattersForSize = false;
        private int endCap = BasicStroke.CAP_SQUARE;
        private int fontStyle = Font.PLAIN;

        private Builder() {
        }

        private Builder(Style style) {
            thickness = style.thickness;
            filled = style.filled;
            colorKey = style.colorKey;
            color = style.color;
            fontSize = style.fontSize;
            dash = style.getDash();
            mattersForSize = style.mattersForSize;
            endCap = style.stroke.getEndCap();
        }

        private Builder setThickness(int thickness) {
            this.thickness = thickness;
            return this;
        }

        private Builder setFilled(boolean filled) {
            this.filled = filled;
            return this;
        }

        private Builder setColor(ColorKey key) {
            this.colorKey = key;
            this.color = null;
            return this;
        }

        private Builder setColor(Color color) {
            this.colorKey = null;
            this.color = color;
            return this;
        }

        private Builder setFontSize(int fontSize) {
            this.fontSize = fontSize;
            return this;
        }

        private Builder setFontStyle(int fontStyle) {
            this.fontStyle = fontStyle;
            return this;
        }

        private Builder setDash(float[] dash) {
            this.dash = dash;
            return this;
        }

        private Builder setMattersForSize(boolean mattersForSize) {
            this.mattersForSize = mattersForSize;
            return this;
        }

        private Builder setEndCap(int endCap) {
            this.endCap = endCap;
            return this;
        }

        private Style build() {
            return new Style(this);
        }

    }

}
