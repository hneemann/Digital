/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics.text.formatter;

import de.neemann.digital.draw.graphics.text.text.*;
import de.neemann.digital.draw.graphics.text.text.Character;

/**
 * The SVG text formatter
 */
public final class SVGFormatter {

    private SVGFormatter() {
    }

    /**
     * Formats the given text
     *
     * @param text the text to format
     * @return the formatted string
     */
    public static String format(Text text) {
        return format(text, false);
    }

    private static String format(Text text, boolean mathMode) {
        if (text instanceof Simple) {
            return ((Simple) text).getText();
        } else if (text instanceof Blank) {
            return " ";
        } else if (text instanceof Character) {
            return character(((Character) text).getChar());
        } else if (text instanceof Decorate) {
            Decorate d = (Decorate) text;
            switch (d.getStyle()) {
                case MATH:
                    if (mathMode)
                        return format(d.getContent(), true);
                    else
                        return "<tspan style=\"font-style:italic;\">" + format(d.getContent(), true) + "</tspan>";
                case OVERLINE:
                    return "<tspan style=\"text-decoration:overline;\">" + format(d.getContent(), mathMode) + "</tspan>";
                default:
                    return format(d.getContent(), mathMode);
            }
        } else if (text instanceof Index) {
            Index i = (Index) text;
            String str = format(i.getVar(), true);
            if (i.getSubScript() != null)
                str += "<tspan style=\"font-size:80%;baseline-shift:sub;\">" + format(i.getSubScript(), mathMode) + "</tspan>";
            if (i.getSuperScript() != null)
                str += "<tspan style=\"font-size:80%;baseline-shift:super;\">" + format(i.getSuperScript(), mathMode) + "</tspan>";
            return str;
        } else if (text instanceof Sentence) {
            Sentence s = (Sentence) text;
            StringBuilder sb = new StringBuilder();
            for (Text t : s)
                sb.append(format(t, mathMode));
            return sb.toString();
        } else return "";
    }

    private static String character(char aChar) {
        switch (aChar) {
            case '&':
                return "&amp;";
            case '<':
                return "&lt;";
            case '>':
                return "&gt;";
            case '"':
                return "&quot;";
            default:
                return "" + aChar;
        }
    }

}
