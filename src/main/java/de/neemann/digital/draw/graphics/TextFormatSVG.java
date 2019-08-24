/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics;

import de.neemann.digital.draw.graphics.text.ParseException;
import de.neemann.digital.draw.graphics.text.Parser;
import de.neemann.digital.draw.graphics.text.formatter.SVGFormatter;
import de.neemann.digital.draw.graphics.text.text.Decorate;
import de.neemann.digital.draw.graphics.text.text.Text;

import java.awt.*;

final class TextFormatSVG implements GraphicSVG.TextStyle {
    @Override
    public String format(String text, Style style) {
        try {
            Text t = new Parser(text).parse();
            if (style.getFontStyle() == Font.ITALIC)
                t = Decorate.math(t);
            return SVGFormatter.format(t);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return text;
    }
}
