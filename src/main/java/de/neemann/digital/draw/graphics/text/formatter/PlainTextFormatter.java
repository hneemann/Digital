/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics.text.formatter;

import de.neemann.digital.analyse.expression.format.FormatToExpression;
import de.neemann.digital.draw.graphics.text.text.*;
import de.neemann.digital.draw.graphics.text.text.Character;

/**
 * Is used to create a simple string
 */
public final class PlainTextFormatter {

    private PlainTextFormatter() {
    }

    /**
     * Creates a simple string
     *
     * @param text the text
     * @return the string representation
     */
    public static String format(Text text) {
        return format(text, FormatToExpression.getDefaultFormat());
    }

    /**
     * Creates a simple string
     *
     * @param text   the text
     * @param format the format to use
     * @return the string representation
     */
    public static String format(Text text, FormatToExpression format) {
        if (text instanceof Simple) {
            return ((Simple) text).getText();
        } else if (text instanceof Blank) {
            return " ";
        } else if (text instanceof Character) {
            return "" + ((Character) text).getChar();
        } else if (text instanceof Decorate) {
            Decorate d = (Decorate) text;
            if (d.getStyle() == Decorate.Style.OVERLINE) {
                final Text content = d.getContent();
                if (content instanceof Simple || content instanceof Index)
                    return format.getNot() + format(content, format);
                else
                    return format.getNot() + "(" + format(content, format) + ")";
            } else
                return format(d.getContent(), format);
        } else if (text instanceof Index) {
            Index i = (Index) text;
            String str = format(i.getVar(), format);
            if (i.getSubScript() != null)
                str += format(i.getSubScript(), format);
            if (i.getSuperScript() != null)
                str += format(i.getSuperScript(), format);
            return str;
        } else if (text instanceof Sentence) {
            Sentence s = (Sentence) text;
            StringBuilder sb = new StringBuilder();
            for (Text t : s)
                sb.append(format(t, format));
            return sb.toString();
        } else return "";
    }

}
