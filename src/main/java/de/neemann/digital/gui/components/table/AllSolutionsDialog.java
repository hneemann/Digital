/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.table;

import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.Screen;
import de.neemann.gui.ToolTipAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

/**
 * Simple Dialog to show all possible functions of a truth table.
 */
public class AllSolutionsDialog extends JDialog {
    private final ExpressionComponent expressionComponent;
    private boolean userHasClosed = false;
    private boolean needed;
    private ToolTipAction reopenAction;

    /**
     * Creates a new Frame.
     *
     * @param owner the owner frame
     * @param font  the font to use
     */
    AllSolutionsDialog(JDialog owner, Font font) {
        super(owner, Lang.get("win_allSolutions"), false);
        setDefaultCloseOperation(HIDE_ON_CLOSE);

        expressionComponent = new ExpressionComponent();
        expressionComponent.setPreferredSize(Screen.getInstance().scale(new Dimension(600, 300)));
        expressionComponent.setFont(font);
        expressionComponent.setBackground(Color.WHITE);

        final JScrollPane scrollPane = new JScrollPane(expressionComponent);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        getContentPane().add(scrollPane);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                userHasClosed = true;
                if (reopenAction != null)
                    reopenAction.setEnabled(true);
            }
        });

        pack();
        setLocation(0, 0);
    }

    /**
     * Is called from table dialog if this dialog is needed.
     *
     * @param needed            true if needed
     * @param tableDialogBounds the table dialogs bounds
     */
    public void setNeeded(boolean needed, Rectangle tableDialogBounds) {
        this.needed = needed;
        if (!userHasClosed) {
            setVisible(needed);
            int x = (int) (tableDialogBounds.x - (getWidth() - tableDialogBounds.getWidth()) / 2);
            int y = tableDialogBounds.y + tableDialogBounds.height + 10;
            if (getHeight() < tableDialogBounds.y - 10) {
                y = tableDialogBounds.y - getHeight() - 10;
            }
            setLocation(x, y);
            if (needed && reopenAction != null)
                reopenAction.setEnabled(false);
        }
    }

    /**
     * Sets the expressions
     *
     * @param expressions the expressions to show
     */
    public void setExpressions(ArrayList<Expression> expressions) {
        expressionComponent.setExpressions(expressions);
    }

    ToolTipAction getReopenAction() {
        if (reopenAction == null) {
            reopenAction = new ToolTipAction(Lang.get("menu_table_showAllSolutions")) {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    userHasClosed = false;
                    reopenAction.setEnabled(false);
                    if (needed)
                        setVisible(true);
                }
            }.setToolTip(Lang.get("menu_table_showAllSolutions_tt")).setEnabledChain(false);
        }
        return reopenAction;
    }
}
