package de.neemann.digital.gui.components.test;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.Signal;
import de.neemann.digital.core.wiring.Clock;
import de.neemann.digital.lang.Lang;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.ArrayList;

/**
 * @author hneemann
 */
public class TestResult implements TableModel {

    private final ArrayList<String> names;
    private final ArrayList<Value[]> lines;
    private final ArrayList<MatchedValue[]> results;
    private ArrayList<TestSignal> inputs;
    private ArrayList<TestSignal> outputs;
    private boolean allPassed;

    /**
     * Creates a new test result
     *
     * @param testData the test data
     */
    public TestResult(TestData testData) {
        names = testData.getNames();
        lines = testData.getLines();
        results = new ArrayList<>();
    }

    /**
     * Creates the result by comparing the test vector with the given model-
     *
     * @param model the model to check
     * @return this for chained calls
     * @throws DataException DataException
     * @throws NodeException NodeException
     */
    public TestResult create(Model model) throws DataException, NodeException {
        allPassed = true;
        inputs = new ArrayList<>();
        outputs = new ArrayList<>();
        for (Signal s : model.getInputs())
            inputs.add(new TestSignal(getIndexOf(s.getName()), s.getValue()));
        for (Clock c : model.getClocks())
            inputs.add(new TestSignal(getIndexOf(c.getClockOutput().getName()), c.getClockOutput()));

        for (Signal s : model.getOutputs())
            outputs.add(new TestSignal(getIndexOf(s.getName()), s.getValue()));

        model.init();

        for (Value[] row : lines) {

            MatchedValue[] res = new MatchedValue[row.length];

            for (TestSignal in : inputs) {
                row[in.index].setTo(in.value);
                res[in.index]=new MatchedValue(row[in.index], in.value);
            }

            model.doStep();

            for (TestSignal out : outputs) {
                MatchedValue matchedValue = new MatchedValue(row[out.index], out.value);
                res[out.index]= matchedValue;
                if (!matchedValue.isPassed())
                    allPassed = false;
            }
            results.add(res);
        }

        return this;
    }

    /**
     * @return true if all tests have passed
     */
    public boolean isAllPassed() {
        return allPassed;
    }

    private int getIndexOf(String name) throws DataException {
        for (int i = 0; i < names.size(); i++) {
            String n = names.get(i);
            if (n.equals(name))
                return i;
        }
        throw new DataException(Lang.get("err_signal_N_notInTextVector", name));
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

    private class TestSignal {
        private final int index;
        private final ObservableValue value;

        TestSignal(int index, ObservableValue value) {
            this.index = index;
            this.value = value;
        }
    }

    @Override
    public int getRowCount() {
        return results.size();
    }

    @Override
    public int getColumnCount() {
        return names.size();
    }

    @Override
    public String getColumnName(int columnIndex) {
        return names.get(columnIndex);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return MatchedValue.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return getValue(rowIndex, columnIndex);
    }

    /**
     * Returns the typed value
     * @param rowIndex rowIndex
     * @param columnIndex columnIndex
     * @return the value
     */
    public MatchedValue getValue(int rowIndex, int columnIndex) {
        return results.get(rowIndex)[columnIndex];
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
