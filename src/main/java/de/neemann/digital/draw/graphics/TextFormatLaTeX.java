/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics;

import de.neemann.digital.draw.graphics.text.ParseException;
import de.neemann.digital.draw.graphics.text.Parser;
import de.neemann.digital.draw.graphics.text.formatter.LaTeXFormatter;
import de.neemann.digital.draw.graphics.text.text.Decorate;
import de.neemann.digital.draw.graphics.text.text.Text;

import java.awt.*;
import java.util.ArrayList;

/**
 * Formats text in LaTeX style.
 */
public class TextFormatLaTeX implements GraphicSVG.TextStyle {
    private static final ArrayList<FontSize> FONT_SIZES = new ArrayList<>();
    private boolean pinStyleInMathMode;

    /**
     * Creates a new instance.
     *
     * @param pinStyleInMathMode if true pin lables are set in math mode
     */
    public TextFormatLaTeX(boolean pinStyleInMathMode) {
        this.pinStyleInMathMode = pinStyleInMathMode;
    }

    private static final class FontSize {

        private final String name;
        private final int size;

        private FontSize(String name, int size) {
            this.name = name;
            this.size = size;
        }
    }

    static {
        add("tiny", 35);     // measured pixel sizes in a BEAMER created PDF
        add("scriptsize", 46);
        add("footnotesize", 52);
        add("small", 58);
        add("normalsize", 63);
        add("large", 69);
        add("Large", 83);
        add("LARGE", 100);
        add("huge", 120);
        add("Huge", 143);
    }

    private static void add(String name, int size) {
        FONT_SIZES.add(new FontSize(name, (size * Style.NORMAL.getFontSize()) / 63));
    }

    private static String getFontSizeName(int fontSize) {
        String best = "normalsize";
        int diff = Integer.MAX_VALUE;
        for (FontSize fs : FONT_SIZES) {
            int d = Math.abs(fontSize - fs.size);
            if (d < diff) {
                diff = d;
                best = fs.name;
            }
        }
        return best;
    }

    @Override
    public String format(String text, Style style) {
        try {
            Text t = new Parser(text).parse();
            if (style.getFontStyle() == Font.ITALIC || (style == Style.SHAPE_PIN && pinStyleInMathMode))
                t = Decorate.math(t);
            text = LaTeXFormatter.format(t);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (style.getFontSize() != Style.NORMAL.getFontSize()) {
            final String fontSizeName = getFontSizeName(style.getFontSize());
            if (!fontSizeName.equals("normalsize"))
                text = "{\\" + fontSizeName + " " + text + "}";
        }
        return GraphicSVG.escapeXML(text);
    }
}
