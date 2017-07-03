package de.neemann.digital.data;

import de.neemann.digital.lang.Lang;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 * The table model to present a test result.
 * <p>
 * Created by hneemann on 24.08.16.
 */
public class ValueTableModel implements TableModel {

    private final ValueTable values;

    /**
     * Creates a new table model
     *
     * @param values the values to wrap
     */
    public ValueTableModel(ValueTable values) {
        this.values = values;
    }

    @Override
    public int getRowCount() {
        return values.getRows();
    }

    @Override
    public int getColumnCount() {
        return values.getColumns() + 1;
    }

    @Override
    public String getColumnName(int columnIndex) {
        if (columnIndex == 0)
            return Lang.get("number");
        else
            return values.getColumnName(columnIndex - 1);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0)
            return Integer.class;
        else
            return Value.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0)
            return rowIndex;
        else
            return values.getValue(rowIndex, columnIndex - 1);
    }


    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    }

    @Override
    public void addTableModelListener(TableModelListener l) {
    }

    @Override
    public void removeTableModelListener(TableModelListener l) {
    }

}
