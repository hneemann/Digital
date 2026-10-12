/*
 * Copyright (c) 2026 Michachatz.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui;

import de.neemann.digital.core.element.Key;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.awt.*;

/**
 * Controls the application's Swing look and feel.
 */
public final class Appearance {

    /**
     * Available application appearances.
     */
    public enum Mode {
        /**
         * Use a light appearance.
         */
        LIGHT,
        /**
         * Use a dark appearance.
         */
        DARK
    }

    /**
     * The selected application appearance.
     */
    public static final Key.KeyEnum<Mode> MODE =
            new Key.KeyEnum<>("appearance", Mode.LIGHT, Mode.values());

    private Appearance() {
    }

    /**
     * Applies the selected appearance to the application.
     *
     * @param mode the appearance to use
     */
    public static void apply(Mode mode) {
        try {
            if (mode == Mode.DARK) {
                MetalLookAndFeel.setCurrentTheme(new DarkMetalTheme());
                UIManager.setLookAndFeel(new MetalLookAndFeel());
            }  else if (mode == Mode.LIGHT) {
                MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme());
                UIManager.setLookAndFeel(new MetalLookAndFeel());
            }
        } catch (UnsupportedLookAndFeelException e) {
            throw new IllegalStateException("Unable to apply appearance " + mode, e);
        }

        for (Window window : Window.getWindows()) {
            SwingUtilities.updateComponentTreeUI(window);
        }
    }

    private static final class DarkMetalTheme extends DefaultMetalTheme {
        private final ColorUIResource primary1 = color(42, 57, 66);
        private final ColorUIResource primary2 = color(57, 76, 88);
        private final ColorUIResource primary3 = color(73, 94, 106);
        private final ColorUIResource secondary1 = color(25, 25, 25);
        private final ColorUIResource secondary2 = color(45, 45, 45);
        private final ColorUIResource secondary3 = color(60, 60, 60);
        private final ColorUIResource lightText = color(238, 238, 238);

        @Override
        protected ColorUIResource getPrimary1() {
            return primary1;
        }

        @Override
        protected ColorUIResource getPrimary2() {
            return primary2;
        }

        @Override
        protected ColorUIResource getPrimary3() {
            return primary3;
        }

        @Override
        protected ColorUIResource getSecondary1() {
            return secondary1;
        }

        @Override
        protected ColorUIResource getSecondary2() {
            return secondary2;
        }

        @Override
        protected ColorUIResource getSecondary3() {
            return secondary3;
        }

        @Override
        protected ColorUIResource getBlack() {
            return lightText;
        }

        @Override
        protected ColorUIResource getWhite() {
            return secondary2;
        }

        private static ColorUIResource color(int red, int green, int blue) {
            return new ColorUIResource(red, green, blue);
        }
    }
}
