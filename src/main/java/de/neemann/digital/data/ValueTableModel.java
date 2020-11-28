/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.data;

import de.neemann.digital.core.Observer;
import de.neemann.digital.testing.parser.TestRow;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.ArrayList;

/**
 * The table model to represent a value table.
 * <p>
 */
public class ValueTableModel implements TableModel, Observer {

    private final ValueTable values;
    private final ArrayList<TableModelListener> listeners;

    /**
     * Creates a new table model
     *
     * @param values the values to wrap
     */
    public ValueTableModel(ValueTable values) {
        this.values = values;
        listeners = new ArrayList<>();
        values.addObserver(this);
    }

    @Override
    public int getRowCount() {
        return values.getTableRows();
    }

    @Override
    public int getColumnCount() {
        return values.getColumns() + 1;
    }

    @Override
    public String getColumnName(int columnIndex) {
        if (columnIndex == 0)
            return "";
        else
            return values.getColumnName(columnIndex - 1);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0)
            return String.class;
        else
            return Value.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            final String description = values.getDescription(rowIndex);
            if (description == null)
                return "";
            else
                return description;
        } else
            return values.getTableValue(rowIndex, columnIndex - 1);
    }


    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    }

    @Override
    public void addTableModelListener(TableModelListener l) {
        listeners.add(l);
    }

    @Override
    public void removeTableModelListener(TableModelListener l) {
        listeners.remove(l);
    }

    @Override
    public void hasChanged() {
        SwingUtilities.invokeLater(() -> {
            TableModelEvent tme = new TableModelEvent(this);
            for (TableModelListener l : listeners)
                l.tableChanged(tme);
        });
    }

    /**
     * Returns a table row
     *
     * @param row the number of the table row
     * @return the table row
     */
    public TestRow getRow(int row) {
        return values.getTableRow(row);
    }
}
