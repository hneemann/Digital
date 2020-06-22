/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics;

import de.neemann.digital.core.element.Key;
import de.neemann.digital.gui.Settings;

import java.awt.*;

/**
 * Color map.
 * Used to define the different color schemes.
 */
public final class ColorMap {

    private static final ColorMap DEFAULT_MAP = new ColorMap()
            .set(ColorKey.BACKGROUND, Color.WHITE)
            .set(ColorKey.MAIN, Color.BLACK)
            .set(ColorKey.WIRE, Color.BLUE.darker())
            .set(ColorKey.WIRE_LOW, new Color(0, 142, 0))
            .set(ColorKey.WIRE_HIGH, new Color(102, 255, 102))
            .set(ColorKey.WIRE_OUT, Color.RED.darker())
            .set(ColorKey.WIRE_VALUE, new Color(50, 162, 50))
            .set(ColorKey.WIRE_Z, Color.GRAY)
            .set(ColorKey.PINS, Color.GRAY)
            .set(ColorKey.HIGHLIGHT, Color.CYAN)
            .set(ColorKey.GRID, new Color(210, 210, 210))
            .set(ColorKey.PASSED, Color.GREEN)
            .set(ColorKey.ERROR, Color.RED);

    private static final ColorMap DARK_MAP = new ColorMap()
            .set(ColorKey.BACKGROUND, Color.BLACK)
            .set(ColorKey.MAIN, Color.GRAY)
            .set(ColorKey.WIRE, Color.BLUE.darker())
            .set(ColorKey.WIRE_LOW, new Color(0, 142, 0))
            .set(ColorKey.WIRE_HIGH, new Color(102, 255, 102))
            .set(ColorKey.WIRE_OUT, Color.RED.darker())
            .set(ColorKey.WIRE_VALUE, new Color(50, 162, 50))
            .set(ColorKey.WIRE_Z, Color.GRAY)
            .set(ColorKey.PINS, Color.GRAY)
            .set(ColorKey.HIGHLIGHT, Color.CYAN)
            .set(ColorKey.GRID, new Color(50, 50, 50))
            .set(ColorKey.PASSED, Color.GREEN)
            .set(ColorKey.ERROR, Color.RED);

    private static final ColorMap COLOR_BLIND_MAP = new ColorMap()
            .set(ColorKey.BACKGROUND, Color.WHITE)
            .set(ColorKey.MAIN, Color.BLACK)
            .set(ColorKey.WIRE, Color.BLUE.darker())
            .set(ColorKey.WIRE_LOW, new Color(32, 59, 232))
            .set(ColorKey.WIRE_HIGH, new Color(244, 235, 66))
            .set(ColorKey.WIRE_OUT, Color.RED.darker())
            .set(ColorKey.WIRE_VALUE, new Color(50, 162, 50))
            .set(ColorKey.WIRE_Z, new Color(1, 188, 157))
            .set(ColorKey.PINS, Color.GRAY)
            .set(ColorKey.HIGHLIGHT, Color.CYAN)
            .set(ColorKey.GRID, new Color(210, 210, 210))
            .set(ColorKey.PASSED, Color.GREEN)
            .set(ColorKey.ERROR, Color.RED);

    private enum ColorSchemes {
        DEFAULT(DEFAULT_MAP), DARK(DARK_MAP), COLOR_BLIND(COLOR_BLIND_MAP);

        private final ColorMap map;

        ColorSchemes(ColorMap map) {
            this.map = map;
        }

        private ColorMap getMap() {
            return map;
        }
    }

    /**
     * The key used to select the color map
     */
    public static final Key<ColorSchemes> COLOR_SCHEME =
            new Key.KeyEnum<>("colorScheme", ColorSchemes.DEFAULT, ColorSchemes.values())
                    .setRequiresRepaint();

    private static ColorMap instance = null;

    /**
     * @return the selected color map
     */
    public static ColorMap getInstance() {
        if (instance == null) {
            Settings.getInstance().getAttributes().addListener(ColorMap::updateInstance);
            updateInstance();
        }
        return instance;
    }

    private static void updateInstance() {
        instance = Settings.getInstance().get(COLOR_SCHEME).getMap();
    }

    /**
     * The identifiers for the different colors
     */
    public enum ColorKey {BACKGROUND, MAIN, WIRE, WIRE_HIGH, WIRE_LOW, WIRE_VALUE, WIRE_OUT, WIRE_Z, ERROR, PASSED, PINS, GRID, HIGHLIGHT}

    private final Color[] colors;

    private ColorMap() {
        colors = new Color[ColorKey.HIGHLIGHT.ordinal() + 1];
    }

    private ColorMap set(ColorKey key, Color color) {
        colors[key.ordinal()] = color;
        return this;
    }

    /**
     * Returns the selected color
     *
     * @param key te color key
     * @return the color
     */
    public Color getColor(ColorKey key) {
        Color color = colors[key.ordinal()];
        if (color == null)
            return colors[ColorKey.MAIN.ordinal()];
        return color;
    }

    /**
     * Provides a color
     */
    public interface ColorProvider {
        /**
         * @return the color
         */
        Color getColor();
    }

    static final class ColorByKey implements ColorProvider {
        private final ColorKey key;

        ColorByKey(ColorKey key) {
            this.key = key;
        }

        @Override
        public Color getColor() {
            return getInstance().getColor(key);
        }
    }
}
