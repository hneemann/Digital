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
 * Stores the test results created by a single {@link TestData} instance.
 * The class also performs the tests.
 * Implemements {@link TableModel}, so the result can easily be shown in a {@link javax.swing.JTable}.
 *
 * @author hneemann
 */
public class TestResult implements TableModel {

    private final ArrayList<String> names;
    private final ArrayList<Value[]> lines;
    private final ArrayList<Value[]> results;
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
        ArrayList<TestSignal> inputs = new ArrayList<>();
        for (Signal s : model.getInputs())
            inputs.add(new TestSignal(getIndexOf(s.getName()), s.getValue()));
        for (Clock c : model.getClocks())
            inputs.add(new TestSignal(getIndexOf(c.getLabel()), c.getClockOutput()));

        ArrayList<TestSignal> outputs = new ArrayList<>();
        for (Signal s : model.getOutputs())
            outputs.add(new TestSignal(getIndexOf(s.getName()), s.getValue()));

        model.init();

        for (Value[] row : lines) {

            Value[] res = new Value[row.length];

            boolean isClockValue = false;
            for (TestSignal in : inputs) {
                if (row[in.index].getType() != Value.Type.CLOCK) {
                    row[in.index].setTo(in.value);
                } else {
                    isClockValue = true;
                }
                res[in.index] = row[in.index];
            }
            if (isClockValue) {

                model.doStep();  // propagate all except clock

                for (TestSignal in : inputs) {
                    if (row[in.index].getType() == Value.Type.CLOCK) {
                        row[in.index].setTo(in.value);
                    }
                }

                model.doStep();  // propagate clock high

                for (TestSignal in : inputs) {  // reset clock values
                    if (row[in.index].getType() == Value.Type.CLOCK) {
                        if (row[in.index].getValue() != 0)
                            in.value.set(0, false);
                        else
                            in.value.set(1, false);
                    }
                }
            }

            model.doStep();

            for (TestSignal out : outputs) {
                MatchedValue matchedValue = new MatchedValue(row[out.index], out.value);
                res[out.index] = matchedValue;
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
    public boolean allPassed() {
        return allPassed;
    }

    private int getIndexOf(String name) throws DataException {
        if (name == null || name.length() == 0)
            throw new DataException(Lang.get("err_unnamedSignal", name));

        for (int i = 0; i < names.size(); i++) {
            String n = names.get(i);
            if (n.equals(name))
                return i;
        }
        throw new DataException(Lang.get("err_signal_N_notInTextVector", name));
    }

    private static class TestSignal {
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
     *
     * @param rowIndex    rowIndex
     * @param columnIndex columnIndex
     * @return the value
     */
    public Value getValue(int rowIndex, int columnIndex) {
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
