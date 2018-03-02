/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.testing;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.Signal;
import de.neemann.digital.core.wiring.Clock;
import de.neemann.digital.data.Value;
import de.neemann.digital.data.ValueTable;
import de.neemann.digital.lang.Lang;
import de.neemann.digital.testing.parser.Context;
import de.neemann.digital.testing.parser.LineEmitter;
import de.neemann.digital.testing.parser.ParserException;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Stores the test results created by a single {@link TestCaseDescription} instance.
 * The class also performs the tests.
 */
public class TestExecutor {
    private static final int MAX_RESULTS = 1 << 10;
    private static final int ERR_RESULTS = MAX_RESULTS * 2;

    private final ArrayList<String> names;
    private final LineEmitter lines;
    private final ValueTable results;
    private boolean allPassed;
    private Exception exception;
    private boolean toManyResults = false;
    private ArrayList<TestSignal> inputs;
    private ArrayList<TestSignal> outputs;

    /**
     * Creates a new testing result
     *
     * @param testCaseDescription the testing data
     * @throws TestingDataException DataException
     */
    public TestExecutor(TestCaseDescription testCaseDescription) throws TestingDataException {
        names = testCaseDescription.getNames();
        results = new ValueTable(names);
        lines = testCaseDescription.getLines();
    }

    /**
     * Creates the result by comparing the testing vector with the given model-
     *
     * @param model the model to check
     * @return this for chained calls
     * @throws TestingDataException DataException
     * @throws NodeException        NodeException
     */
    public TestExecutor create(Model model) throws TestingDataException, NodeException {
        allPassed = true;
        HashSet<String> usedSignals = new HashSet<>();

        inputs = new ArrayList<>();
        outputs = new ArrayList<>();
        for (Signal s : model.getInputs()) {
            final int index = getIndexOf(s.getName());
            if (index >= 0) {
                inputs.add(new TestSignal(index, s.getValue()));
                addTo(usedSignals, s.getName());
            }
            ObservableValue outValue = s.getBidirectionalReader();
            if (outValue != null) {
                final String outName = s.getName() + "_out";
                final int inIndex = getIndexOf(outName);
                if (inIndex >= 0) {
                    outputs.add(new TestSignal(inIndex, outValue));
                    addTo(usedSignals, outName);
                }
            }
        }

        for (Clock c : model.getClocks()) {
            final int index = getIndexOf(c.getLabel());
            if (index >= 0) {
                inputs.add(new TestSignal(index, c.getClockOutput()));
                addTo(usedSignals, c.getLabel());
            }
        }

        for (Signal s : model.getOutputs()) {
            final int index = getIndexOf(s.getName());
            if (index >= 0) {
                outputs.add(new TestSignal(index, s.getValue()));
                addTo(usedSignals, s.getName());
            }
        }

        for (String name : names)
            if (!usedSignals.contains(name))
                throw new TestingDataException(Lang.get("err_testSignal_N_notFound", name));

        if (inputs.size() == 0)
            throw new TestingDataException(Lang.get("err_noTestInputSignalsDefined"));

        if (outputs.size() == 0)
            throw new TestingDataException(Lang.get("err_noTestOutputSignalsDefined"));

        model.init();

        try {
            lines.emitLines(new LineListenerResolveDontCare(values -> checkRow(model, values), inputs), new Context());
        } catch (ParserException e) {
            throw new TestingDataException(Lang.get("err_errorParsingTestdata"), e);
        } catch (RuntimeException e) {
            if (allPassed) {
                allPassed = false;
                exception = e;
            }
        }

        return this;
    }

    private void addTo(HashSet<String> signals, String name) throws TestingDataException {
        if (signals.contains(name))
            throw new TestingDataException(Lang.get("err_nameUsedTwice_N", name));
        signals.add(name);
    }

    private void checkRow(Model model, Value[] row) {
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

        try {
            if (clockIsUsed) {  // a clock signal is used
                model.doStep();  // propagate all except clock
                addClockRow(row.length);

                // set clock
                for (TestSignal in : inputs)
                    if (row[in.index].getType() == Value.Type.CLOCK) {
                        row[in.index].copyTo(in.value);
                        res[in.index] = row[in.index];
                    }

                // propagate clock change
                model.doStep();
                addClockRow(row.length);

                // restore clock
                for (TestSignal in : inputs)   // invert the clock values
                    if (row[in.index].getType() == Value.Type.CLOCK) {
                        in.value.setBool(!in.value.getBool());
                        res[in.index] = new Value(in.value);
                    }
            }

            model.doStep();
        } catch (NodeException | RuntimeException e) {
            exception = e;
            allPassed = false;
            throw new RuntimeException(e);
        }

        boolean ok = true;
        for (TestSignal out : outputs) {
            MatchedValue matchedValue = new MatchedValue(row[out.index], out.value);
            res[out.index] = matchedValue;
            if (!matchedValue.isPassed()) {
                allPassed = false;
                ok = false;
            }
        }

        if (results.getRows() < (ok ? MAX_RESULTS : ERR_RESULTS))
            results.add(res);
        else
            toManyResults = true;
    }

    private void addClockRow(int cols) {
        if (results.getRows() < ERR_RESULTS) {
            Value[] r = new Value[cols];
            for (TestSignal out : outputs)
                r[out.index] = new Value(out.value);
            for (TestSignal in : inputs)
                r[in.index] = new Value(in.value);
            results.add(r).omitInTable();
        } else
            toManyResults = true;
    }

    /**
     * @return true if all tests have passed
     */
    public boolean allPassed() {
        return allPassed;
    }


    /**
     * Indicates if there are to many entries in the table to show.
     * If there are to many entries, the test results is still correct.
     *
     * @return true if there are missing items in the results list.
     */
    public boolean toManyResults() {
        return toManyResults;
    }

    private int getIndexOf(String name) {
        if (name == null || name.length() == 0)
            return -1;

        for (int i = 0; i < names.size(); i++) {
            String n = names.get(i);
            if (n.equals(name))
                return i;
        }
        return -1;
    }

    /**
     * @return return the result
     */
    public ValueTable getResult() {
        return results;
    }

    /**
     * A test signal
     */
    public static class TestSignal {
        private final int index;
        private final ObservableValue value;

        TestSignal(int index, ObservableValue value) {
            this.index = index;
            this.value = value;
        }

        /**
         * @return the index of this value
         */
        public int getIndex() {
            return index;
        }
    }

    /**
     * @return the exception thrown during test test execution, or null if there was no error.
     */
    public Exception getException() {
        return exception;
    }
}
