package de.neemann.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Class used to handle diifferent screen resolution by defining a new default font
 * used by the GUI components.
 * Created by hneemann on 09.05.17.
 */
public final class Screen {

    private static final class InstanceHolder {
        private static Screen instance = new Screen();
    }

    private final float size;
    private final float scaling;
    private final Font font;

    /**
     * @return the Screen instance
     */
    public static Screen getInstance() {
        return InstanceHolder.instance;
    }

    private Screen() {
        Font font = new JLabel().getFont();
        float scaling = 1;
        float size = 12;
        try {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            float s = screenSize.height / 90f;
            if (s > 12) {
                font = font.deriveFont(s);
                for (Object key : javax.swing.UIManager.getLookAndFeel().getDefaults().keySet()) {
                    if (key.toString().endsWith(".font"))
                        javax.swing.UIManager.put(key, font);
                }
                scaling = s / 12;
                size = s;
                UIManager.put("ScrollBar.width", (int) (size * 17 / 12));
            }
        } catch (HeadlessException e) {
            // run with defaults if headless
        }
        this.scaling = scaling;
        this.size = size;
        this.font = font;
    }

    /**
     * @return font size
     */
    public float getFontSize() {
        return size;
    }

    /**
     * @return the font
     */
    public Font getFont() {
        return font;
    }

    /**
     * Get scaled font
     *
     * @param scale the scaling factor
     * @return the scaled font
     */
    public Font getFont(float scale) {
        return font.deriveFont(font.getSize2D() * scale);
    }

    /**
     * @return the scaling
     */
    public float getScaling() {
        return scaling;
    }

    /**
     * Scales the given dimension
     *
     * @param dimension the given dimension
     * @return the scaled dimension
     */
    public Dimension scale(Dimension dimension) {
        if (scaling == 1)
            return dimension;
        else
            return new Dimension((int) (dimension.width * scaling), (int) (dimension.height * scaling));
    }

}
