/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.table;

import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.draw.graphics.text.formatter.GraphicsFormatter;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

import static de.neemann.digital.draw.graphics.text.formatter.GraphicsFormatter.createFragment;

/**
 * Component to show an expression
 */
public class ExpressionComponent extends JComponent {
    private static final int XPAD = 5;
    private ArrayList<Expression> expressions;
    private boolean wrongSize = true;

    /**
     * Sets a single expression to visualize
     *
     * @param expression the expression
     */
    public void setExpression(Expression expression) {
        ArrayList<Expression> l = new ArrayList<>();
        if (expression != null)
            l.add(expression);
        setExpressions(l);
    }

    /**
     * Sets the expressions to visualize
     *
     * @param expressions expressions
     */
    public void setExpressions(ArrayList<Expression> expressions) {
        this.expressions = expressions;
        updateComponentSize(getGraphics());
    }

    /**
     * Updates the components size
     *
     * @param gr the Graphics instance to use
     */
    protected void updateComponentSize(Graphics gr) {
        if (gr != null) {
            final Dimension preferredSize = calcSize(gr);
            setPreferredSize(preferredSize);
            revalidate();
            repaint();
            wrongSize = false;
        } else {
            wrongSize = true;
        }
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        graphics.setColor(getBackground());
        graphics.fillRect(0, 0, getWidth(), getHeight());
        graphics.setColor(getForeground());

        if (expressions == null)
            return;

        final Graphics2D gr = getGraphics2D(graphics);
        int lineSpacing = getFont().getSize() / 2;
        int y = 0;
        for (Expression e : expressions) {
            try {
                GraphicsFormatter.Fragment fr = createFragment(gr, e);
                y += fr.getHeight();
                fr.draw(gr, XPAD, y);
                y += lineSpacing;
            } catch (GraphicsFormatter.FormatterException ex) {
                // ignore on error
            }
        }

        if (wrongSize)
            SwingUtilities.invokeLater(() -> updateComponentSize(graphics));
    }

    private Graphics2D getGraphics2D(Graphics graphics) {
        final Graphics2D gr = (Graphics2D) graphics;
        gr.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        gr.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        return gr;
    }

    private Dimension calcSize(Graphics graphics) {
        Graphics2D gr = getGraphics2D(graphics);
        int lineSpacing = gr.getFont().getSize() / 2;
        int dx = 0;
        int y = 0;
        for (Expression e : expressions) {
            try {
                GraphicsFormatter.Fragment fr = createFragment(gr, e);
                y += fr.getHeight() + lineSpacing;
                if (dx < fr.getWidth())
                    dx = fr.getWidth();
            } catch (GraphicsFormatter.FormatterException ex) {
                // ignore on error
            }
        }
        return new Dimension(dx + XPAD * 2, y);
    }

}
