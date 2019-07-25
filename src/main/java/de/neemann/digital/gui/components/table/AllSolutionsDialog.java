/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.table;

import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.Screen;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

/**
 * Simple Dialog to show all possible functions of a truth table.
 */
public class AllSolutionsDialog extends JDialog {
    private final ExpressionComponent expressionComponent;
    private boolean userHasClosed = false;

    /**
     * Creates a new Frame.
     *
     * @param owner the owner frame
     * @param font  the font to use
     */
    public AllSolutionsDialog(JDialog owner, Font font) {
        super(owner, Lang.get("win_allSolutions"), false);
        setDefaultCloseOperation(HIDE_ON_CLOSE);

        expressionComponent = new ExpressionComponent();
        expressionComponent.setPreferredSize(Screen.getInstance().scale(new Dimension(600, 300)));
        expressionComponent.setFont(font);

        final JScrollPane scrollPane = new JScrollPane(expressionComponent);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        getContentPane().add(scrollPane);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                userHasClosed = true;
            }
        });

        pack();
        setLocation(0, 0);
    }

    /**
     * Is called from table dialog if this dialog is needed.
     *
     * @param needed true if needed
     */
    public void setNeeded(boolean needed) {
        if (!userHasClosed)
            setVisible(needed);
    }

    /**
     * Sets the expressions
     *
     * @param expressions the expressions to show
     */
    public void setExpressions(ArrayList<Expression> expressions) {
        expressionComponent.setExpressions(expressions);
    }


}
