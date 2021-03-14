/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics.text.formatter;

import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.format.FormatToExpression;
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
     * @param exp the expression to format
     * @return the formatted string
     */
    public static String format(Expression exp) {
        return format(new ExpressionToText().createText(exp, FormatToExpression.LATEX), true);
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
                    if (mathMode)
                        return "\\overline{" + format(d.getContent(), true) + "}";
                    else {
                        final Text c = d.getContent();
                        if (c instanceof Index)
                            return "$\\overline{" + format(c, true) + "}$";
                        else
                            return "$\\overline{\\mbox{" + format(c, true) + "}}$";
                    }
                default:
                    return format(d.getContent(), mathMode);
            }
        } else if (text instanceof Index) {
            Index i = (Index) text;
            String str = format(i.getVar(), true);
            if (i.getSuperScript() != null)
                str += '^' + brace(format(i.getSuperScript(), true));
            if (i.getSubScript() != null)
                str += '_' + brace(format(i.getSubScript(), true));
            if (mathMode)
                return str;
            else
                return "$" + str + "$";
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
                if (inMath)
                    return "\\neg{}";
                else
                    return "$\\neg{}$";
            case '\u2265':
                if (inMath)
                    return "\\geq\\!\\!{}";
                else
                    return "$\\geq\\!\\!{}$";
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
                if (inMath)
                    return "\\ \\&\\ ";
                else
                    return "\\&";
            case '|':
                if (inMath)
                    return "\\ |\\ ";
                else
                    return "|";
            case '_':
                return "\\_";
            case '\\':
                return "\\\\";
            case '∑':
                if (inMath)
                    return "\\sum ";
                else
                    return "$\\sum$";
            case '∏':
                if (inMath)
                    return "\\prod ";
                else
                    return "$\\prod$";
            case '∧':
                if (inMath)
                    return "\\wedge ";
                else
                    return "$\\wedge$";
            case '∨':
                if (inMath)
                    return "\\vee ";
                else
                    return "$\\vee$";
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
