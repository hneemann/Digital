/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.table;

import de.neemann.digital.analyse.TruthTable;
import de.neemann.digital.analyse.TruthTableTableModel;
import de.neemann.digital.analyse.expression.ExpressionException;
import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.analyse.quinemc.BoolTable;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 * Handles reordering of the table columns by mouse drag and drop
 */
public class TableReorderManager {

    private final TableDialog tableDialog;
    private final JTable table;

    /**
     * creates a new instance
     *
     * @param tableDialog the TableDialog instance
     * @param table       the table which is reordered
     */
    public TableReorderManager(TableDialog tableDialog, JTable table) {
        this.tableDialog = tableDialog;
        this.table = table;
        table.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
                if (mouseEvent.getButton() == 1) {
                    TableModel m = table.getModel();
                    if (m instanceof TruthTableTableModel)
                        checkReorder((TruthTableTableModel) m);
                }
            }
        });
    }

    private void checkReorder(TruthTableTableModel model) {
        ArrayList<Variable> varList = model.getTable().getVars();
        ArrayList<String> vars = new ArrayList<>();
        ArrayList<String> results = new ArrayList<>();
        boolean wasChange = false;
        for (int i = 0; i < model.getColumnCount(); i++) {
            String name = table.getTableHeader().getColumnModel().getColumn(i).getHeaderValue().toString();
            if (!name.equals(table.getModel().getColumnName(i)))
                wasChange = true;
            BoolTable r = model.getTable().getResult(name);
            if (r != null && varList.contains(new Variable(name)))
                return;

            if (r != null) results.add(name);
            else vars.add(name);
        }

        if (wasChange) {
            if (isVarChange(varList, vars)) {
                try {
                    TruthTable tt = new ReorderInputs(model.getTable(), vars).reorder();
                    tableDialog.setModel(new TruthTableTableModel(tt));
                } catch (ExpressionException e) {
                    // can't happen because no columns are removed
                    e.printStackTrace();
                }
            } else if (isResultChange(model.getTable(), results)) {
                try {
                    TruthTable tt = new ReorderOutputs(model.getTable(), results).reorder();
                    tableDialog.setModel(new TruthTableTableModel(tt));
                } catch (ExpressionException e) {
                    // can't happen because no columns are removed
                    e.printStackTrace();
                }
            } else
                tableDialog.setModel(new TruthTableTableModel(model.getTable()));
        }
    }

    private boolean isResultChange(TruthTable table, ArrayList<String> results) {
        if (table.getResultCount() != results.size())
            return false;

        for (int i = 0; i < results.size(); i++)
            if (!table.getResultName(i).equals(results.get(i)))
                return true;

        return false;
    }

    private boolean isVarChange(ArrayList<Variable> varList, ArrayList<String> vars) {
        if (varList.size() != vars.size())
            return false;

        for (int i = 0; i < varList.size(); i++)
            if (!varList.get(i).getIdentifier().equals(vars.get(i)))
                return true;

        return false;
    }

}
