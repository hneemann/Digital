package de.neemann.digital.gui.components.testing;

import de.neemann.digital.lang.Lang;
import de.neemann.digital.testing.MatchedValue;
import de.neemann.digital.testing.TestResult;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 * The table model to present a test result.
 * <p>
 * Created by hneemann on 24.08.16.
 */
public class TestResultModel implements TableModel {

    private final TestResult testResult;

    /**
     * Creates a new table model
     *
     * @param testResult the testresult to wrap
     */
    public TestResultModel(TestResult testResult) {
        this.testResult = testResult;
    }

    @Override
    public int getRowCount() {
        return testResult.getRows();
    }

    @Override
    public int getColumnCount() {
        return testResult.getSignalCount() + 1;
    }

    @Override
    public String getColumnName(int columnIndex) {
        if (columnIndex == 0)
            return Lang.get("number");
        else
            return testResult.getSignalName(columnIndex - 1);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0)
            return Integer.class;
        else
            return MatchedValue.class;
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
            return testResult.getResultValue(rowIndex, columnIndex - 1);
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
