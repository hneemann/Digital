package de.neemann.digital.gui.components.test;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.ArrayList;

/**
 * @author hneemann
 */
public class TestResult implements TableModel {

    private final ArrayList<String> names;
    private final ArrayList<Line> lines;

    /**
     * Creates a new test result
     *
     * @param testData the test data
     */
    public TestResult(TestData testData) {
        names = testData.getNames();
        lines = new ArrayList<>();
    }

    @Override
    public int getRowCount() {
        return lines.size();
    }

    @Override
    public int getColumnCount() {
        return names.size() + 1;
    }

    @Override
    public String getColumnName(int columnIndex) {
        if (columnIndex < names.size())
            return names.get(columnIndex);
        else
            return "";
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex < names.size())
            return Integer.class;
        else
            return Boolean.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return lines.get(rowIndex).getCol(columnIndex);
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

    private class Line {
        private final int[] data;
        private final boolean passed;

        Line(int[] data, boolean passed) {
            this.data = data;
            this.passed = passed;
        }

        Object getCol(int columnIndex) {
            if (columnIndex < names.size())
                return data[columnIndex];
            else
                return passed;
        }
    }
}
