/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.gui;

/*
 * Win 150% : getScreenResolution() = 144
 * Win 100% : getScreenResolution() = 96
 * Linux    : getScreenResolution() = 95
 */

import de.neemann.digital.core.element.Keys;
import de.neemann.digital.gui.Settings;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * Class used to handle different screen resolution by defining a new default font
 * used by the GUI components. Also all the icons are scaled.
 */
public final class Screen {
    private static final String OS = System.getProperty("os.name").toLowerCase();
    private static final boolean IS_LINUX = OS.contains("linux");
    private static final boolean IS_MAC = OS.contains("mac");

    private static final class InstanceHolder {
        private static Screen instance = new Screen();
    }

    private final int size;
    private final float scaling;
    private final Font font;

    /**
     * @return the Screen instance
     */
    public static Screen getInstance() {
        return InstanceHolder.instance;
    }


    private static int getDefaultScreenResolution() {
        try {
            int dpi = Toolkit.getDefaultToolkit().getScreenResolution();

            // plausibility check
            int widthInPixel = Toolkit.getDefaultToolkit().getScreenSize().width;
            int widthInInch = widthInPixel / dpi;
            // most people don't use a screen larger than 27 inch, so the resolution is presumably wrong
            if (widthInInch > 27)
                // assume a 27 inch screen
                dpi = widthInPixel / 27;

            return dpi;
        } catch (HeadlessException e) {
            return 95;
        }
    }

    /**
     * @return the default font scaling in percent
     */
    public static int getDefaultFontScaling() {
        if (IS_MAC)   // macOS has its own retina handling
            return 100;

        int dpi = getDefaultScreenResolution();
        int s = (dpi * 100) / 96;
        if (s > 95 && s < 105)
            s = 100;
        return s;
    }

    private Screen() {
        Font font = new JLabel().getFont();
        float scaling = 1;
        int size = font.getSize();
        int fontScalingPercent = Settings.getInstance().get(Keys.SETTINGS_FONT_SCALING);
        int s = fontScalingPercent * size / 100;
        if (s != size) {
            scaling = ((float) s) / size;
            size = s;
            font = font.deriveFont((float) s);
            for (Object key : javax.swing.UIManager.getLookAndFeel().getDefaults().keySet()) {
                if (key.toString().endsWith(".font"))
                    javax.swing.UIManager.put(key, font);

                if (!IS_MAC) { // macOS has its own icon handling
                    if (key.toString().endsWith(".icon") || key.toString().endsWith("Icon")) {
                        Icon icon = UIManager.getIcon(key);
                        if (icon != null)
                            javax.swing.UIManager.put(key, new ScaleIcon(icon, scaling));
                    }
                }
            }
            UIManager.put("ScrollBar.width", size * 17 / 12);
        }
        this.scaling = scaling;
        this.size = size;
        this.font = font;
    }

    private static final class ScaleIcon implements Icon {
        private final Icon icon;
        private final float scaling;
        private final int width;
        private final int height;

        private ScaleIcon(Icon icon, float scaling) {
            this.icon = icon;
            this.scaling = scaling;
            width = (int) (icon.getIconWidth() * scaling);
            height = (int) (icon.getIconHeight() * scaling);
        }

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D gr = (Graphics2D) graphics;
            AffineTransform tr = gr.getTransform();
            gr.translate(x, y);
            gr.scale(scaling, scaling);
            gr.translate(-x, -y);
            icon.paintIcon(component, gr, x, y);
            gr.setTransform(tr);
        }

        @Override
        public int getIconWidth() {
            return width;
        }

        @Override
        public int getIconHeight() {
            return height;
        }
    }

    /**
     * @return font size
     */
    public int getFontSize() {
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

    /**
     * @return true if running on a windows system
     */
    public static boolean isLinux() {
        return IS_LINUX;
    }

    /**
     * @return true if running on a mac
     */
    public static boolean isMac() {
        return IS_MAC;
    }

    /**
     * Sets the location of a window.
     * Ensures that the window is completely visible on the screen the given position belongs to.
     * The window is centered relative to the given position.
     *
     * @param w      the window
     * @param pos    the position
     * @param center if true the window is centered
     */
    public static void setLocation(Window w, Point pos, boolean center) {
        if (pos == null)
            return;

        Rectangle screen = null;
        GraphicsDevice[] dev = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
        for (GraphicsDevice d : dev) {
            for (GraphicsConfiguration c : d.getConfigurations()) {
                Rectangle b = c.getBounds();
                if (b.contains(pos))
                    screen = b;
            }
        }

        if (center) {
            pos.x -= w.getWidth() / 2;
            pos.y -= w.getHeight() / 2;
        }

        if (screen != null) {
            if (pos.x + w.getWidth() > screen.x + screen.width) pos.x = screen.x + screen.width - w.getWidth();
            if (pos.y + w.getHeight() > screen.y + screen.height) pos.y = screen.y + screen.height - w.getHeight();
            if (pos.x < screen.x) pos.x = screen.x;
            if (pos.y < screen.y) pos.y = screen.y;
        }

        w.setLocation(pos.x, pos.y);
    }

}
