package de.neemann.digital.draw.graphics.text.formatter;

import de.neemann.digital.draw.graphics.text.text.*;
import de.neemann.digital.draw.graphics.text.text.Character;

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
     * Draws the given text.
     *
     * @param gr   the {@link Graphics2D} instance
     * @param x    the x position
     * @param y    the y position
     * @param text the text
     * @throws FormatterException FormatterException
     */
    public static void draw(Graphics2D gr, int x, int y, Text text) throws FormatterException {
        Fragment f = createFragment(gr, gr.getFont(), text);
        draw(gr, x, y, f);
    }

    /**
     * Draws the given text.
     *
     * @param gr       the {@link Graphics2D} instance
     * @param x        the x position
     * @param y        the y position
     * @param fragment the text fragment
     */
    public static void draw(Graphics2D gr, int x, int y, Fragment fragment) {
        Font font = gr.getFont();
        Stroke stroke = gr.getStroke();
        gr.setStroke(new BasicStroke(2f));
        fragment.draw(gr, x, y);
        gr.setFont(font);
        gr.setStroke(stroke);
    }

    /**
     * Creates the text fragments
     *
     * @param gr   the {@link Graphics2D} instance
     * @param text the text
     * @return the text fragment
     * @throws FormatterException FormatterException
     */
    public static Fragment createFragment(Graphics2D gr, Text text) throws FormatterException {
        return createFragment(gr, gr.getFont(), text);
    }

    private static Fragment createFragment(Graphics2D gr, Font font, Text text) throws FormatterException {
        if (text instanceof Simple) {
            return new TextFragment(gr, font, ((Simple) text).getText());
        } else if (text instanceof Character) {
            return new TextFragment(gr, font, "" + ((Character) text).getChar());
        } else if (text instanceof Sentence) {
            Sentence s = (Sentence) text;
            SentenceFragment sf = new SentenceFragment();
            int x = 0;
            for (Text t : s)
                if (t instanceof Blank)
                    x += gr.getFont().getSize() / 2;
                else {
                    final Fragment f = createFragment(gr, font, t);
                    f.x = x;
                    x += f.dx;
                    sf.add(f);
                }
            sf.dx = x;
            return sf.setUp();
        } else if (text instanceof Index) {
            Index i = (Index) text;
            Fragment var = createFragment(gr, font, i.getVar());
            Font f = font.deriveFont(font.getSize() / 1.4f);
            Fragment superScript = i.getSuperScript() == null ? null : createFragment(gr, f, i.getSuperScript());
            Fragment subScript = i.getSubScript() == null ? null : createFragment(gr, f, i.getSubScript());
            return new IndexFragment(var, superScript, subScript);
        } else if (text instanceof Decorate) {
            Decorate d = (Decorate) text;
            switch (d.getStyle()) {
                case MATH:
                    return createFragment(gr, font.deriveFont(Font.ITALIC), d.getContent());
                case OVERLINE:
                    return new OverlineFragment(createFragment(gr, font, d.getContent()), font.getSize() / 10);
                default:
                    return createFragment(gr, font, d.getContent());
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

        void set(int dx, int dy, int base) {
            this.dx = dx;
            this.dy = dy;
            this.base = base;
        }

        void draw(Graphics2D gr, int xOfs, int yOfs) {
//            gr.drawRect(xOfs + x, yOfs + y + base - dy, dx, dy);
//            gr.drawLine(xOfs + x, yOfs + y, xOfs + x + dx, yOfs + y);
        }

        /**
         * @return the width of this fragment
         */
        public int getWidth() {
            return dx;
        }
    }

    private final static class TextFragment extends Fragment {
        private final String text;
        private final Font font;

        private TextFragment(Graphics2D gr, Font font, String text) {
            this.font = font;
            final FontMetrics metrics = gr.getFontMetrics(font);
            Rectangle2D rec = metrics.getStringBounds(text, gr);
            set((int) rec.getWidth(), (int) rec.getHeight(), metrics.getDescent());
            this.text = text;
        }

        @Override
        void draw(Graphics2D gr, int xOfs, int yOfs) {
            super.draw(gr, xOfs, yOfs);
            gr.setFont(font);
            gr.drawString(text, x + xOfs, y + yOfs);
        }
    }

    private final static class SentenceFragment extends Fragment {

        private ArrayList<Fragment> fragments;

        private SentenceFragment() {
            this.fragments = new ArrayList<>();
        }

        private void add(Fragment fragment) {
            fragments.add(fragment);
        }

        @Override
        void draw(Graphics2D gr, int xOfs, int yOfs) {
            super.draw(gr, xOfs, yOfs);
            for (Fragment f : fragments)
                f.draw(gr, x + xOfs, y + yOfs);
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
            int ofs = var.dy / 6;
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
        void draw(Graphics2D gr, int xOfs, int yOfs) {
            super.draw(gr, xOfs, yOfs);
            var.draw(gr, xOfs + x, yOfs + y);
            if (superScript != null)
                superScript.draw(gr, xOfs + x, yOfs + y);
            if (subScript != null)
                subScript.draw(gr, xOfs + x, yOfs + y);
        }
    }

    private final static class OverlineFragment extends Fragment {
        private final Fragment fragment;
        private final int indent;

        private OverlineFragment(Fragment fragment, int indent) {
            this.fragment = fragment;
            this.indent = indent;
            this.dx = fragment.dx;
            this.dy = fragment.dy;
            this.base = fragment.base;
        }

        @Override
        void draw(Graphics2D gr, int xOfs, int yOfs) {
            super.draw(gr, xOfs, yOfs);
            fragment.draw(gr, xOfs + x, yOfs + y);
            int yy = yOfs + y - dy + base;
            gr.drawLine(xOfs + x + indent, yy, xOfs + x + dx - indent / 2, yy);
        }
    }
}
