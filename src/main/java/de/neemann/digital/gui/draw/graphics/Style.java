package de.neemann.digital.gui.draw.graphics;

import java.awt.*;

/**
 * @author hneemann
 */
public class Style {
    public static final Style NORMAL = new Style(1, false, Color.BLACK);
    public static final Style WIRE = new Style(2, false, Color.BLACK);
    public static final Style FILLED = new Style(1, true, Color.BLACK);

    private final int thickness;
    private final boolean filled;

    private Style(int thickness, boolean filled, Color color) {
        this.thickness = thickness;
        this.filled = filled;
    }

    public int getThickness() {
        return thickness;
    }

    public boolean isFilled() {
        return filled;
    }
}
