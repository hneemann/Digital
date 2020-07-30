/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics;

import java.awt.*;

/**
 * Creates a high contrast style best for printing
 */
public class ColorStyleHighContrast implements GraphicSVG.ColorStyle {
    @Override
    public Color getColor(Style style) {
        if (style == Style.WIRE) return Style.NORMAL.getColor();
        else if (style == Style.WIRE_OUT) return Style.NORMAL.getColor();
        else if (style == Style.WIRE_BITS) return Style.NORMAL.getColor();
        else if (style == Style.SHAPE_PIN) return Style.NORMAL.getColor();
        else if (style == Style.SHAPE_SPLITTER) return Style.NORMAL.getColor();
        else return style.getColor();
    }
}
