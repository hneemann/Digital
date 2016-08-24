package de.neemann.digital.testing;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.Signal;
import de.neemann.digital.core.wiring.Clock;
import de.neemann.digital.lang.Lang;

import java.util.ArrayList;

/**
 * Stores the test results created by a single {@link TestData} instance.
 * The class also performs the tests.
 *
 * @author hneemann
 */
public class TestResult {

    private final ArrayList<String> names;
    private final ArrayList<Value[]> lines;
    private final ArrayList<Value[]> results;
    private boolean allPassed;

    /**
     * Creates a new testing result
     *
     * @param testData the testing data
     */
    public TestResult(TestData testData) {
        names = testData.getNames();
        lines = testData.getLines();
        results = new ArrayList<>();
    }

    /**
     * Creates the result by comparing the testing vector with the given model-
     *
     * @param model the model to check
     * @return this for chained calls
     * @throws TestingDataException DataException
     * @throws NodeException NodeException
     */
    public TestResult create(Model model) throws TestingDataException, NodeException {
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

            boolean clockIsUsed = false;
            // set all values except the clocks
            for (TestSignal in : inputs) {
                if (row[in.index].getType() != Value.Type.CLOCK) {
                    row[in.index].copyTo(in.value);
                } else {
                    clockIsUsed = true;
                }
                res[in.index] = row[in.index];
            }

            if (clockIsUsed) {  // a clock signal is used
                model.doStep();  // propagate all except clock

                // set clock
                for (TestSignal in : inputs)
                    if (row[in.index].getType() == Value.Type.CLOCK)
                        row[in.index].copyTo(in.value);

                // propagate clock change
                model.doStep();

                // restore clock
                for (TestSignal in : inputs)   // invert the clock values
                    if (row[in.index].getType() == Value.Type.CLOCK)
                        in.value.setBool(!in.value.getBool());
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

    private int getIndexOf(String name) throws TestingDataException {
        if (name == null || name.length() == 0)
            throw new TestingDataException(Lang.get("err_unnamedSignal", name));

        for (int i = 0; i < names.size(); i++) {
            String n = names.get(i);
            if (n.equals(name))
                return i;
        }
        throw new TestingDataException(Lang.get("err_signal_N_notInTestVector", name));
    }

    /**
     * @return the number of rows
     */
    public int getRows() {
        return results.size();
    }

    /**
     * @return the number of signals
     */
    public int getSignalCount() {
        return names.size();
    }

    /**
     * returns a signal name
     *
     * @param index the index of the requested signals name
     * @return the signals name
     */
    public String getSignalName(int index) {
        return names.get(index);
    }

    /**
     * Returns the typed value
     *
     * @param rowIndex    rowIndex
     * @param columnIndex columnIndex
     * @return the value
     */
    public Value getResultValue(int rowIndex, int columnIndex) {
        return results.get(rowIndex)[columnIndex];
    }

    private static class TestSignal {
        private final int index;
        private final ObservableValue value;

        TestSignal(int index, ObservableValue value) {
            this.index = index;
            this.value = value;
        }
    }

}
