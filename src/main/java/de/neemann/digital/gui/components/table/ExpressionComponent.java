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
    private ArrayList<Expression> expressions;
    private Dimension lastRectSet;


    /**
     * Sets a single expression to visualize
     *
     * @param expression the expression
     */
    public void setExpression(Expression expression) {
        ArrayList<Expression> l = new ArrayList<Expression>();
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
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, getWidth(), getHeight());
        graphics.setColor(Color.BLACK);

        if (expressions == null)
            return;

        final Graphics2D gr = (Graphics2D) graphics;
        gr.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        gr.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int lineSpacing = getFont().getSize() / 2;
        int y = 0;
        int dx = 0;
        for (Expression e : expressions) {
            try {
                GraphicsFormatter.Fragment fr = createFragment(gr, e);
                y += fr.getHeight();
                fr.draw(gr, 5, y);
                y += lineSpacing;

                if (dx < fr.getWidth())
                    dx = fr.getWidth();

            } catch (GraphicsFormatter.FormatterException ex) {
            }
        }

        Dimension p = new Dimension(dx, y);
        if (!p.equals(lastRectSet)) {
            lastRectSet = p;
            SwingUtilities.invokeLater(() -> {
                setPreferredSize(p);
                revalidate();
            });
        }
    }
}
