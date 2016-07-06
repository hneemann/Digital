package de.neemann.digital.gui.components.test;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
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
    private final ArrayList<Line> lines;
    private final ArrayList<int[]> values;
    private ArrayList<TestInput> inputs;
    private ArrayList<TestOutput> outputs;
    private boolean allPassed;

    /**
     * Creates a new test result
     *
     * @param testData the test data
     */
    public TestResult(TestData testData) {
        names = testData.getNames();
        values = testData.getLines();
        lines = new ArrayList<>();
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
            inputs.add(new TestSignal(s, getIndexOf(s.getName())));
        for (Clock c : model.getClocks())
            inputs.add(new TestClock(c, getIndexOf(c.getClockOutput().getName())));

        for (Signal s : model.getOutputs())
            outputs.add(new TestSignal(s, getIndexOf(s.getName())));

        model.init();

        for (int[] row : values) {
            for (TestInput in : inputs)
                in.setFrom(row);

            model.doStep();

            boolean ok = true;
            for (TestOutput out : outputs) {
                if (!out.check(row)) {
                    ok = false;
                    allPassed = false;
                }
            }
            lines.add(new Line(row, ok));
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

    private interface TestInput {
        void setFrom(int[] row);
    }

    private interface TestOutput {
        boolean check(int[] row);
    }

    private class TestSignal implements TestInput, TestOutput {
        private final Signal s;
        private final int index;

        TestSignal(Signal s, int index) {
            this.s = s;
            this.index = index;
        }

        @Override
        public void setFrom(int[] row) {
            s.getValue().setValue(row[index]);
        }

        @Override
        public boolean check(int[] row) {
            int val = row[index];
            return val < 0 || val == s.getValue().getValue();
        }
    }

    private class TestClock implements TestInput {
        private final Clock c;
        private final int index;

        TestClock(Clock c, int index) {
            this.c = c;
            this.index = index;
        }

        @Override
        public void setFrom(int[] row) {
            c.getClockOutput().setValue(row[index]);
        }
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
}
