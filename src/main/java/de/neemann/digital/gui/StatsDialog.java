/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui;

import de.neemann.digital.lang.Lang;
import de.neemann.gui.Screen;

import javax.swing.*;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.*;

/**
 * Dialog used to show the circuits stats
 */
public class StatsDialog extends JDialog {

    /**
     * Creates a new instance
     *
     * @param frame the parent frame
     * @param model the table model
     */
    public StatsDialog(Frame frame, TableModel model) {
        super(frame, Lang.get("menu_stats"));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        final JTable table = new JTable(model);
        getContentPane().add(new JScrollPane(table));
        final TableColumnModel columnModel = table.getColumnModel();
        final int fontSize = Screen.getInstance().getFontSize();
        columnModel.getColumn(0).setPreferredWidth(fontSize * 35);
        columnModel.getColumn(1).setPreferredWidth(fontSize * 6);
        columnModel.getColumn(2).setPreferredWidth(fontSize * 6);
        columnModel.getColumn(3).setPreferredWidth(fontSize * 8);
        table.setPreferredScrollableViewportSize(new Dimension(fontSize * 55, fontSize * 40));

        pack();
        setLocationRelativeTo(frame);
    }
}
