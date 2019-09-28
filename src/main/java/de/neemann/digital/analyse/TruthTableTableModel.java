/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse;

import de.neemann.digital.analyse.quinemc.BoolTable;
import de.neemann.digital.undo.ModifyException;
import de.neemann.digital.undo.UndoManager;

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

    private final ArrayList<TableModelListener> listeners = new ArrayList<>();
    private final UndoManager<TruthTable> undoManager;

    /**
     * Creates a new instance
     *
     * @param undoManager the undoManager
     */
    public TruthTableTableModel(UndoManager<TruthTable> undoManager) {
        this.undoManager = undoManager;
    }

    @Override
    public int getRowCount() {
        return undoManager.getActual().getRows();
    }

    @Override
    public int getColumnCount() {
        return undoManager.getActual().getCols();
    }

    @Override
    public String getColumnName(int columnIndex) {
        return undoManager.getActual().getColumnName(columnIndex);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return Integer.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return undoManager.getActual().isEditable(columnIndex);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return undoManager.getActual().getValue(rowIndex, columnIndex);
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (aValue instanceof Integer)
            setValue(rowIndex, columnIndex, (Integer) aValue);
        if (aValue instanceof String) {
            if (aValue.toString().equals("0"))
                setValue(rowIndex, columnIndex, 0);
            else if (aValue.toString().equals("1"))
                setValue(rowIndex, columnIndex, 1);
            else
                setValue(rowIndex, columnIndex, 2);
        }
    }

    private void setValue(int rowIndex, int columnIndex, int val) {
        int actVal = undoManager.getActual().getValue(rowIndex, columnIndex);
        if (actVal != val) {
            try {
                undoManager.apply(truthTable -> truthTable.setValue(rowIndex, columnIndex, val));
            } catch (ModifyException e) {
                e.printStackTrace();
            }
            fireModelEvent(rowIndex);
        }
    }

    private void fireModelEvent(int rowIndex) {
        TableModelEvent e = new TableModelEvent(this, rowIndex);
        for (TableModelListener l : listeners)
            l.tableChanged(e);
    }

    /**
     * Fires a structural table change
     */
    public void fireTableChanged() {
        TableModelEvent e = new TableModelEvent(this, HEADER_ROW);
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
     * Sets the column name
     *
     * @param columnIndex the column
     * @param name        the new name
     */
    public void setColumnName(int columnIndex, String name) {
        try {
            undoManager.apply(truthTable -> truthTable.setColumnName(columnIndex, name));
        } catch (ModifyException e) {
            e.printStackTrace();
        }
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
        TruthTable tt = undoManager.getActual();
        for (int i = 0; i < tt.getResultCount(); i++) {
            if (tt.getResult(i) == boolTable) {
                col = i;
                break;
            }
        }
        if (col >= 0) {
            col += tt.getVars().size();
            int value = tt.getValue(row, col);
            if (value == 2) value = 0;
            else value++;
            setValueAt(value, row, col);
        }
    }

    /**
     * @return the truth table shown
     */
    public TruthTable getTable() {
        return undoManager.getActual();
    }
}
