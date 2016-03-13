package de.neemann.digital.gui.draw.graphics;

/**
 * @author hneemann
 */
public class Style {
    public static final Style NORMAL = new Style(1);
    public static final Style WIRE = new Style(2);

    private final int thickness;

    private Style(int thickness) {
        this.thickness = thickness;
    }

    public int getThickness() {
        return thickness;
    }
}
