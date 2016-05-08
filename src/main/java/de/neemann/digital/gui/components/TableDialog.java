package de.neemann.digital.gui.components;

import de.neemann.digital.analyse.TruthTable;
import de.neemann.digital.analyse.TruthTableTableModel;
import de.neemann.digital.lang.Lang;

import javax.swing.*;
import java.awt.*;

/**
 * Dialog to visualise a truth table.
 *
 * @author hneemann
 */
public class TableDialog extends JDialog {
    /**
     * Creates a new instance
     *
     * @param owner      the owner of this dialog
     * @param truthTable the truth table
     */
    public TableDialog(Frame owner, TruthTable truthTable) {
        super(owner, Lang.get("win_table"));
        JTable table = new JTable(new TruthTableTableModel(truthTable));
        table.setPreferredScrollableViewportSize(table.getPreferredSize());
        getContentPane().add(new JScrollPane(table));
        pack();
        setLocationRelativeTo(owner);
    }
}
