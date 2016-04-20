package de.neemann.digital.gui.components;

import de.neemann.digital.analyse.ModelAnalyser;
import de.neemann.digital.analyse.ModelAnalyzerTableModel;
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
     * @param owner    the owner of this dialog
     * @param analyzer the truth table
     */
    public TableDialog(Frame owner, ModelAnalyser analyzer) {
        super(owner, Lang.get("win_table"));
        JTable table = new JTable(new ModelAnalyzerTableModel(analyzer));
        table.setPreferredScrollableViewportSize(table.getPreferredSize());
        getContentPane().add(new JScrollPane(table));
        pack();
        setLocationRelativeTo(owner);
    }
}
