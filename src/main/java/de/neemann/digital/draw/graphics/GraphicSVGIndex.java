/*
 * Copyright (c) 2016 Helmut Neemann
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
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Subclass of {@link GraphicSVG} which creates the correct SVG representation
 * of an index if used like "x_0".
 */
public class GraphicSVGIndex extends GraphicSVG {

    /**
     * Creates new instance
     *
     * @param out the file
     * @throws IOException IOException
     */
    public GraphicSVGIndex(OutputStream out) throws IOException {
        super(out);
    }

    /**
     * Creates new instance
     *
     * @param out      the output stream to use
     * @param source   source file, only used to create a comment in the SVG file
     * @param svgScale the scaling
     * @throws IOException IOException
     */
    public GraphicSVGIndex(OutputStream out, File source, int svgScale) throws IOException {
        super(out, source, svgScale);
    }

    @Override
    public String formatText(String text, Style style) {
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
