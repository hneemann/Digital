package de.neemann.digital.analyse;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 * Used to visualize a truthTable instance in a JTable
 *
 * @author hneemann
 */
public class TruthTableTableModel implements TableModel {
    private final TruthTable truthTable;

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
    }

    @Override
    public void addTableModelListener(TableModelListener l) {
    }

    @Override
    public void removeTableModelListener(TableModelListener l) {
    }
}
