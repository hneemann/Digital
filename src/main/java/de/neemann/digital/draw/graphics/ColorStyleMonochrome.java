/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics;

import java.awt.*;

/**
 * Generates monochrome colors.
 */
public class ColorStyleMonochrome implements GraphicSVG.ColorStyle {

    private GraphicSVG.ColorStyle parent;

    /**
     * Creates a new instance
     *
     * @param parent the parent color style
     */
    public ColorStyleMonochrome(GraphicSVG.ColorStyle parent) {
        this.parent = parent;
    }

    @Override
    public Color getColor(Style style) {
        Color color = parent.getColor(style);
        int c = (color.getBlue() + color.getRed() + color.getGreen()) / 3;
        return new Color(c, c, c, color.getAlpha());
    }
}
