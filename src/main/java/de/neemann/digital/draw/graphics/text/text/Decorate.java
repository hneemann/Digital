/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics.text.text;

/**
 * Decorates a text fragment
 */
public class Decorate implements Text {

    /**
     * Adds a MATH decoration to the given text.
     * If the text is already decorated the text is returned directly.
     *
     * @param t the text to decorate
     * @return the decorated text
     */
    public static Text math(Text t) {
        if (t instanceof Decorate) {
            Decorate d = (Decorate) t;
            if (d.getStyle() == Style.MATH)
                return t;
        }
        return new Decorate(t, Style.MATH);
    }


    /**
     * The different styles
     */
    public enum Style {
        /**
         * Normal
         */
        NORMAL,
        /**
         * overline
         */
        OVERLINE,
        /**
         * Math mode
         */
        MATH
    }

    private Text content;
    private final Style style;

    /**
     * Creates a new decorator
     *
     * @param content the content to decorate
     * @param style   the decoration style
     */
    public Decorate(Text content, Style style) {
        this.content = content;
        this.style = style;
    }

    /**
     * @return the content
     */
    public Text getContent() {
        return content;
    }

    /**
     * @return the docoration style
     */
    public Style getStyle() {
        return style;
    }

    @Override
    public Text simplify() {
        if (style.equals(Style.NORMAL))
            return content;
        else
            return this;
    }

    @Override
    public String toString() {
        return "Decorate{" + content + ", " + style + '}';
    }
}
