package de.neemann.digital.analyse;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 * Used to visualize a analyzer instanve in a JTable
 *
 * @author hneemann
 */
public class ModelAnalyzerTableModel implements TableModel {
    private final ModelAnalyser analyzer;

    /**
     * Creates a new instance
     *
     * @param analyzer the analyzer which is to visualize
     */
    public ModelAnalyzerTableModel(ModelAnalyser analyzer) {
        this.analyzer = analyzer;
    }

    @Override
    public int getRowCount() {
        return analyzer.getRows();
    }

    @Override
    public int getColumnCount() {
        return analyzer.getCols();
    }

    @Override
    public String getColumnName(int columnIndex) {
        if (columnIndex < analyzer.getInputs().size())
            return analyzer.getInputs().get(columnIndex).getName();
        else
            return analyzer.getOutputs().get(columnIndex - analyzer.getInputs().size()).getName();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return Integer.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return analyzer.getValue(rowIndex, columnIndex);
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
