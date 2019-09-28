/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.table;

import de.neemann.digital.lang.Lang;
import de.neemann.gui.Screen;

import javax.swing.*;
import java.awt.*;

/**
 * A simple progress dialog which is used by the {@link ExpressionCreator}
 */
public class ProgressDialog extends JDialog implements ExpressionCreator.ProgressListener {
    private final JProgressBar bar;
    private int prog;

    /**
     * Create a new instance
     *
     * @param tableDialog the table dialog
     */
    public ProgressDialog(TableDialog tableDialog) {
        super(tableDialog, false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        bar = new JProgressBar(0, tableDialog.getUndoManager().getActual().getResultCount());
        int b = Screen.getInstance().getFontSize();
        bar.setBorder(BorderFactory.createEmptyBorder(b, b, b, b));
        final JLabel label = new JLabel(Lang.get("msg_optimizationInProgress"));
        label.setBorder(BorderFactory.createEmptyBorder(b, b, 0, b));
        getContentPane().add(label, BorderLayout.NORTH);
        getContentPane().add(bar, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(tableDialog);
        SwingUtilities.invokeLater(() -> setVisible(true));
    }

    @Override
    public void oneCompleted() {
        SwingUtilities.invokeLater(() -> bar.setValue(++prog));
    }

    @Override
    public void complete() {
        SwingUtilities.invokeLater(this::dispose);
    }
}
