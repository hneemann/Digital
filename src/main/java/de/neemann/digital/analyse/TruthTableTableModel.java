/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse;

import de.neemann.digital.analyse.quinemc.BoolTable;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.ArrayList;

import static javax.swing.event.TableModelEvent.HEADER_ROW;

/**
 * Used to visualize a truthTable instance in a JTable
 */
public class TruthTableTableModel implements TableModel {
    /**
     * String representation of the states
     */
    public static final String[] STATENAMES = new String[]{"0", "1", "x"};

    private final TruthTable truthTable;
    private final ArrayList<TableModelListener> listeners = new ArrayList<>();

    /**
     * Creates a new instance
     *
     * @param truthTable the truthTable which is to visualize
     */
    public TruthTableTableModel(TruthTable truthTable) {
        this.truthTable = truthTable;
    }

    @Override
    public int getRowCount() {
        return truthTable.getRows();
    }

    @Override
    public int getColumnCount() {
        return truthTable.getCols();
    }

    @Override
    public String getColumnName(int columnIndex) {
        return truthTable.getColumnName(columnIndex);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return Integer.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return truthTable.isEditable(columnIndex);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return truthTable.getValue(rowIndex, columnIndex);
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (aValue instanceof Integer)
            truthTable.setValue(rowIndex, columnIndex, (Integer) aValue);
        if (aValue instanceof String) {
            if (aValue.toString().equals("0"))
                truthTable.setValue(rowIndex, columnIndex, 0);
            else if (aValue.toString().equals("1"))
                truthTable.setValue(rowIndex, columnIndex, 1);
            else
                truthTable.setValue(rowIndex, columnIndex, 2);
        }
        fireModelEvent(rowIndex);
    }

    private void fireModelEvent(int rowIndex) {
        TableModelEvent e = new TableModelEvent(this, rowIndex);
        for (TableModelListener l : listeners)
            l.tableChanged(e);
    }

    @Override
    public void addTableModelListener(TableModelListener l) {
        listeners.add(l);
    }

    @Override
    public void removeTableModelListener(TableModelListener l) {
    }

    /**
     * @return the truth table used by this model
     */
    public TruthTable getTable() {
        return truthTable;
    }

    /**
     * Sets the column name
     *
     * @param columnIndex the column
     * @param name        the new name
     */
    public void setColumnName(int columnIndex, String name) {
        truthTable.setColumnName(columnIndex, name);
        fireModelEvent(HEADER_ROW);
    }

    /**
     * Changes the value in the given row in the given bool table.
     *
     * @param boolTable the table to modify
     * @param row       the row to modify
     */
    public void incValue(BoolTable boolTable, int row) {
        int col = -1;
        for (int i = 0; i < truthTable.getResultCount(); i++) {
            if (truthTable.getResult(i) == boolTable) {
                col = i;
                break;
            }
        }
        if (col >= 0) {
            col += truthTable.getVars().size();
            int value = truthTable.getValue(row, col);
            if (value == 2) value = 0;
            else value++;
            setValueAt(value, row, col);
        }
    }
}
