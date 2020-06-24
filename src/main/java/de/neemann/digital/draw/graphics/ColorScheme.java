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
public final class ColorScheme {

    private static final ColorScheme DEFAULT_SCHEME = new ColorScheme()
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
            .set(ColorKey.ERROR, Color.RED)
            .set(ColorKey.DISABLED, Color.LIGHT_GRAY)
            .set(ColorKey.TESTCASE, new Color(180, 255, 180, 200))
            .set(ColorKey.ASYNC, new Color(255, 180, 180, 200));

    private static final ColorScheme DARK_SCHEME = new ColorScheme(DEFAULT_SCHEME)
            .set(ColorKey.BACKGROUND, Color.BLACK)
            .set(ColorKey.MAIN, Color.GRAY)
            .set(ColorKey.GRID, new Color(50, 50, 50))
            .set(ColorKey.DISABLED, new Color(40, 40, 40));

    private static final ColorScheme COLOR_BLIND_SCHEME = new ColorScheme(DEFAULT_SCHEME)
            .set(ColorKey.WIRE_LOW, new Color(32, 59, 232))
            .set(ColorKey.WIRE_HIGH, new Color(244, 235, 66))
            .set(ColorKey.WIRE_Z, new Color(1, 188, 157));

    enum ColorSchemes {
        DEFAULT(DEFAULT_SCHEME), DARK(DARK_SCHEME), COLOR_BLIND(COLOR_BLIND_SCHEME);

        private final ColorScheme scheme;

        ColorSchemes(ColorScheme scheme) {
            this.scheme = scheme;
        }

        ColorScheme getScheme() {
            return scheme;
        }
    }

    /**
     * The key used to select the color map
     */
    public static final Key<ColorSchemes> COLOR_SCHEME =
            new Key.KeyEnum<>("colorScheme", ColorSchemes.DEFAULT, ColorSchemes.values())
                    .setRequiresRepaint();

    private static ColorScheme instance = null;

    /**
     * @return the selected color map
     */
    public static ColorScheme getInstance() {
        if (instance == null) {
            updateInstance();
            Settings.getInstance().getAttributes().addListener(ColorScheme::updateInstance);
        }
        return instance;
    }

    private static void updateInstance() {
        instance = Settings.getInstance().get(COLOR_SCHEME).getScheme();
    }

    private final Color[] colors;

    private ColorScheme() {
        colors = new Color[ColorKey.values().length];
    }

    private ColorScheme(ColorScheme other) {
        this.colors = other.colors.clone();
    }

    private ColorScheme set(ColorKey key, Color color) {
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
        return colors[key.ordinal()];
    }
}
