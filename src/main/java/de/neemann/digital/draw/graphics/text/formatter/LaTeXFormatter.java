package de.neemann.digital.draw.graphics.text.formatter;

import de.neemann.digital.draw.graphics.text.text.*;
import de.neemann.digital.draw.graphics.text.text.Character;

/**
 * The LaTeX formatter
 */
public final class LaTeXFormatter {

    private LaTeXFormatter() {
    }

    /**
     * Formats the given text
     *
     * @param text the text to format
     * @return the formatted string
     */
    public static String format(Text text) {
        return format(text.enforceMath(), false);
    }

    private static String format(Text text, boolean mathMode) {
        if (text instanceof Simple) {
            return ((Simple) text).getText();
        } else if (text instanceof Blank) {
            return " ";
        } else if (text instanceof Character) {
            return character(((Character) text).getChar(), mathMode);
        } else if (text instanceof Decorate) {
            Decorate d = (Decorate) text;
            switch (d.getStyle()) {
                case MATH:
                    if (mathMode)
                        return format(d.getContent(), true);
                    else
                        return "$" + format(d.getContent(), true) + "$";
                case OVERLINE:
                    return "\\overline{" + format(d.getContent(), mathMode) + "}";
                default:
                    return format(d.getContent(), mathMode);
            }
        } else if (text instanceof Index) {
            Index i = (Index) text;
            String str = format(i.getVar(), mathMode);
            if (i.getSuperScript() != null)
                str += '^' + brace(format(i.getSuperScript(), mathMode));
            if (i.getSubScript() != null)
                str += '_' + brace(format(i.getSubScript(), mathMode));
            return str;
        } else if (text instanceof Sentence) {
            Sentence s = (Sentence) text;
            StringBuilder sb = new StringBuilder();
            for (Text t : s)
                sb.append(format(t, mathMode));
            return sb.toString();
        } else return "";
    }

    private static String character(char aChar, boolean inMath) {
        switch (aChar) {
            case '\u00AC':
                return "\\neg{}";
            case '\u2265':
                return "\\geq\\!\\!{}";
            case '<':
                if (inMath)
                    return "" + aChar;
                else
                    return "\\textless{}";
            case '>':
                if (inMath)
                    return "" + aChar;
                else
                    return "\\textgreater{}";
            case '&':
                return "\\&";
            default:
                return "" + aChar;
        }
    }

    private static String brace(String format) {
        if (format.length() == 1)
            return format;
        else
            return "{" + format + "}";
    }

}
