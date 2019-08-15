/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics.text.formatter;

import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.draw.graphics.text.text.ExpressionToText;
import de.neemann.digital.draw.graphics.text.ParseException;
import de.neemann.digital.draw.graphics.text.Parser;
import de.neemann.digital.draw.graphics.text.text.Character;
import de.neemann.digital.draw.graphics.text.text.*;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

/**
 * Formatter to draw a text on a {@link Graphics2D} instance.
 */
public final class GraphicsFormatter {

    private GraphicsFormatter() {
    }

    /**
     * Creates the text fragments
     *
     * @param gr   the {@link Graphics2D} instance
     * @param text the text
     * @return the text fragment
     */
    public static Fragment createFragment(Graphics2D gr, String text) {
        return createFragment((fragment, font, str) -> {
            final FontMetrics metrics = gr.getFontMetrics(font);
            Rectangle2D rec = metrics.getStringBounds(str, gr);
            fragment.set((int) rec.getWidth(), (int) rec.getHeight(), metrics.getDescent());
        }, gr.getFont(), text);
    }

    /**
     * Creates the text fragments
     *
     * @param gr         the {@link Graphics2D} instance
     * @param expression the expression
     * @return the text fragment
     * @throws FormatterException FormatterException
     */
    public static Fragment createFragment(Graphics2D gr, Expression expression) throws FormatterException {
        return createFragment((fragment, font, str) -> {
            final FontMetrics metrics = gr.getFontMetrics(font);
            Rectangle2D rec = metrics.getStringBounds(str, gr);
            fragment.set((int) rec.getWidth(), (int) rec.getHeight(), metrics.getDescent());
        }, gr.getFont(), new ExpressionToText().createText(expression));
    }

    /**
     * Creates the text fragments
     *
     * @param sizer the sizer instance
     * @param font  the font
     * @param text  the text
     * @return the fragment
     */
    public static Fragment createFragment(FontSizer sizer, Font font, String text) {
        Fragment fragment;
        try {
            Text t = new Parser(text).parse();
            fragment = createFragment(sizer, font, t);
        } catch (ParseException | FormatterException e) {
            // if there was an exception, return the complete raw text as a fragment
            fragment = new TextFragment(sizer, font, text);
        }
        return fragment;
    }

    /**
     * Creates the text fragments
     *
     * @param sizer the sizer instance
     * @param font  the font
     * @param text  the text
     * @return the fragment
     * @throws FormatterException FormatterException
     */
    private static Fragment createFragment(FontSizer sizer, Font font, Text text) throws FormatterException {
        if (text instanceof Simple) {
            return new TextFragment(sizer, font, ((Simple) text).getText());
        } else if (text instanceof Character) {
            return new TextFragment(sizer, font, "" + ((Character) text).getChar());
        } else if (text instanceof Sentence) {
            Sentence s = (Sentence) text;
            SentenceFragment sf = new SentenceFragment();
            for (Text t : s)
                if (t instanceof Blank)
                    sf.pad(font.getSize() / 2);
                else {
                    final Fragment f = createFragment(sizer, font, t);
                    sf.add(f);
                }
            return sf.setUp();
        } else if (text instanceof Index) {
            Index i = (Index) text;
            Fragment var = createFragment(sizer, font, i.getVar());
            Font f = font.deriveFont(font.getSize() / 1.4f);
            Fragment superScript = i.getSuperScript() == null ? null : createFragment(sizer, f, i.getSuperScript());
            Fragment subScript = i.getSubScript() == null ? null : createFragment(sizer, f, i.getSubScript());
            return new IndexFragment(var, superScript, subScript);
        } else if (text instanceof Decorate) {
            Decorate d = (Decorate) text;
            switch (d.getStyle()) {
                case MATH:
                    return createFragment(sizer, font.deriveFont(Font.ITALIC), d.getContent());
                case OVERLINE:
                    return new OverlineFragment(createFragment(sizer, font, d.getContent()), font);
                default:
                    return createFragment(sizer, font, d.getContent());
            }
        } else
            throw new FormatterException("unknown text element " + text.getClass().getSimpleName() + ", " + text);
    }

    /**
     * Exception which indicates a formatter exception
     */
    public static final class FormatterException extends Exception {
        FormatterException(String message) {
            super(message);
        }
    }

    /**
     * The base class of all text fragments.
     */
    public static abstract class Fragment {
        //CHECKSTYLE.OFF: VisibilityModifier
        protected int x;
        protected int y;
        protected int dx;
        protected int dy;
        protected int base;
        //CHECKSTYLE.ON: VisibilityModifier

        private Fragment() {
        }

        /**
         * Sets the size of this fragment
         *
         * @param dx   width
         * @param dy   height
         * @param base base line
         */
        public void set(int dx, int dy, int base) {
            this.dx = dx;
            this.dy = dy;
            this.base = base;
        }

        void drawDirect(Graphics2D gr, int xOfs, int yOfs) {
//            gr.setStroke(new BasicStroke());
//            gr.drawRect(xOfs + x, yOfs + y + base - dy, dx, dy);
//            gr.drawLine(xOfs + x, yOfs + y, xOfs + x + dx, yOfs + y);
        }

        /**
         * Draws the given text.
         *
         * @param gr the {@link Graphics2D} instance
         * @param x  the x position
         * @param y  the y position
         */
        public void draw(Graphics2D gr, int x, int y) {
            Font font = gr.getFont();
            Stroke stroke = gr.getStroke();
            drawDirect(gr, x, y);
            gr.setFont(font);
            gr.setStroke(stroke);
        }

        /**
         * @return the width of this fragment
         */
        public int getWidth() {
            return dx;
        }

        /**
         * @return the height of this fragment
         */
        public int getHeight() {
            return dy;
        }
    }

    final static class TextFragment extends Fragment {
        private final String text;
        private final Font font;

        private TextFragment(FontSizer sizer, Font font, String text) {
            this.font = font;
            this.text = text;
            sizer.setSizeTo(this, font, text);
        }

        @Override
        void drawDirect(Graphics2D gr, int xOfs, int yOfs) {
            super.drawDirect(gr, xOfs, yOfs);
            gr.setFont(font);
            gr.drawString(text, x + xOfs, y + yOfs);
        }
    }

    private static final class SentenceFragment extends Fragment {

        private ArrayList<Fragment> fragments;

        private SentenceFragment() {
            this.fragments = new ArrayList<>();
        }

        private void add(Fragment fragment) {
            fragments.add(fragment);
            fragment.x = dx;
            dx += fragment.dx;
        }

        private void pad(int p) {
            dx += p;
        }

        @Override
        void drawDirect(Graphics2D gr, int xOfs, int yOfs) {
            super.drawDirect(gr, xOfs, yOfs);
            for (Fragment f : fragments)
                f.drawDirect(gr, x + xOfs, y + yOfs);
        }

        public Fragment setUp() {
            int maxBase = 0;
            int maxAscent = 0;
            for (Fragment f : fragments) {
                if (maxBase < f.base)
                    maxBase = f.base;
                if (maxAscent < f.dy - f.base)
                    maxAscent = f.dy - f.base;
            }
            dy = maxBase + maxAscent;
            base = maxBase;
            return this;
        }
    }

    private final static class IndexFragment extends Fragment {
        private final Fragment var;
        private final Fragment superScript;
        private final Fragment subScript;

        private IndexFragment(Fragment var, Fragment superScript, Fragment subScript) {
            this.var = var;
            this.superScript = superScript;
            this.subScript = subScript;

            if (subScript != null && superScript != null)
                dx = var.dx + Math.max(subScript.dx, superScript.dx);
            else if (subScript != null)
                dx = var.dx + subScript.dx;
            else if (superScript != null)
                dx = var.dx + superScript.dx;
            else
                dx = var.dx;

            dy = var.dy;

            int delta = var.dy / 3;
            int ofs = var.dy / 8;
            if (superScript != null) {
                superScript.x = var.dx;
                superScript.y = -delta - ofs;

                int h = -superScript.y + superScript.dy - superScript.base;
                if (h > var.dy - var.base)
                    dy += h - (var.dy - var.base);
            }
            if (subScript != null) {
                subScript.x = var.dx;
                subScript.y = +delta - ofs;

                int b = subScript.y + subScript.base;
                if (b > var.base) {
                    base = b;
                    dy += b - var.base;
                } else
                    base = var.base;
            }
        }

        @Override
        void drawDirect(Graphics2D gr, int xOfs, int yOfs) {
            super.drawDirect(gr, xOfs, yOfs);
            var.drawDirect(gr, xOfs + x, yOfs + y);
            if (superScript != null)
                superScript.drawDirect(gr, xOfs + x, yOfs + y);
            if (subScript != null)
                subScript.drawDirect(gr, xOfs + x, yOfs + y);
        }
    }

    private final static class OverlineFragment extends Fragment {
        private final Fragment fragment;
        private final float fontSize;
        private final int border;
        private int dx1;
        private int dx2;

        private OverlineFragment(Fragment fragment, Font font) {
            this.fragment = fragment;
            this.fontSize = font.getSize();
            this.dx = fragment.dx;
            border = (int) (fontSize / 5);
            this.dy = fragment.dy + border;
            this.base = fragment.base;
            int indent = dx < fontSize / 2 ? 0 : (int) fontSize / 10;
            dx1 = indent;
            dx2 = indent / 2;
            if (font.getStyle() == Font.ITALIC) {
                dx1 += fontSize / 15;
                dx2 -= fontSize / 15;
            }
        }

        @Override
        void drawDirect(Graphics2D gr, int xOfs, int yOfs) {
            super.drawDirect(gr, xOfs, yOfs);
            fragment.drawDirect(gr, xOfs + x, yOfs + y);
            int yy = yOfs + y - dy + base + border;
            if (fontSize < 15) yy -= 1;
            gr.setStroke(new BasicStroke(fontSize / 10f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
            gr.drawLine(xOfs + x + dx1, yy, xOfs + x + dx - dx2, yy);
        }
    }

    /**
     * Used to determine the size of a string
     */
    public interface FontSizer {
        /**
         * Must set the size of the given fragment by calling the {@link Fragment#set(int, int, int)} method.
         *
         * @param fragment fragment which size is requested
         * @param font     the used font
         * @param str      the string to measure
         */
        void setSizeTo(Fragment fragment, Font font, String str);
    }
}
