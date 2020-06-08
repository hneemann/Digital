/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics;

import de.neemann.digital.core.element.Key;
import de.neemann.digital.gui.SettingsBase;

import java.util.ArrayList;
import java.util.List;

/**
 * Settings used by the SVG exporter.
 */
public final class SVGSettings extends SettingsBase {

    static final Key<Boolean> HIGH_CONTRAST =
            new Key<>("SVG_highContrast", false);
    static final Key<Boolean> MONOCHROME =
            new Key<>("SVG_monochrome", false);
    static final Key<Boolean> SMALL_IO =
            new Key<>("SVG_smallIO", false);
    static final Key<Boolean> NO_PIN_MARKER =
            new Key<>("SVG_noPinMarker", false);
    static final Key<Boolean> THINNER_LINES =
            new Key<>("SVG_thinnerLines", false);
    static final Key<Boolean> HIDE_TEST =
            new Key<>("SVG_hideTest", false);
    static final Key<Boolean> NO_SHAPE_FILLING =
            new Key<>("SVG_noShapeFilling", false);
    static final Key<Boolean> LATEX =
            new Key<>("SVG_LaTeX", false);
    static final Key<Boolean> PINS_IN_MATH_MODE =
            new Key<>("SVG_pinsInMathMode", false).setDependsOn(LATEX);

    private static final class SettingsHolder {
        static final SVGSettings INSTANCE = new SVGSettings();
    }

    /**
     * Returns the settings instance
     *
     * @return the Settings
     */
    public static SVGSettings getInstance() {
        return SettingsHolder.INSTANCE;
    }

    private SVGSettings() {
        super(createKeyList(), ".svgStyle.cfg");
    }

    /**
     * @return a list of available SVG export keys
     */
    public static List<Key> createKeyList() {
        ArrayList<Key> list = new ArrayList<>();
        list.add(LATEX);
        list.add(PINS_IN_MATH_MODE);
        list.add(HIDE_TEST);
        list.add(NO_SHAPE_FILLING);
        list.add(SMALL_IO);
        list.add(NO_PIN_MARKER);
        list.add(THINNER_LINES);
        list.add(HIGH_CONTRAST);
        list.add(MONOCHROME);
        return list;
    }

}
