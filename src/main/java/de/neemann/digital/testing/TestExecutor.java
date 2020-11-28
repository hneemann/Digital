/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.testing;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.wiring.Clock;
import de.neemann.digital.data.Value;
import de.neemann.digital.data.ValueTable;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.draw.library.ResolveGenerics;
import de.neemann.digital.draw.model.ModelCreator;
import de.neemann.digital.lang.Lang;
import de.neemann.digital.testing.parser.*;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Runs the test and stores the test results created by a single {@link TestCaseDescription} instance.
 */
public class TestExecutor {
    private static final int MAX_RESULTS = 1 << 10;
    private static final int ERR_RESULTS = MAX_RESULTS * 2;

    private final ArrayList<String> names;
    private final Model model;
    private final LineEmitter lines;
    private final ValueTable results;
    private final Context context;
    private final ArrayList<TestSignal> inputs;
    private final ArrayList<TestSignal> outputs;
    private boolean errorOccurred;
    private int failedCount;
    private int passedCount;
    private int rowCount;
    private boolean toManyResults = false;
    private int visibleRows;
    private boolean allowMissingInputs;

    /**
     * Creates a new testing result.
     *
     * @param testCase the testing data
     * @param circuit  the circuit
     * @param library  the library
     * @throws TestingDataException     DataException
     * @throws ElementNotFoundException ElementNotFoundException
     * @throws PinException             PinException
     * @throws NodeException            NodeException
     */
    public TestExecutor(Circuit.TestCase testCase, Circuit circuit, ElementLibrary library) throws TestingDataException, NodeException, ElementNotFoundException, PinException {
        this(testCase.getTestCaseDescription(), createModel(testCase, circuit, library));
    }

    static private Model createModel(Circuit.TestCase testCase, Circuit circuit, ElementLibrary library) throws NodeException, ElementNotFoundException, PinException {
        final Model model;
        if (circuit != null && circuit.getAttributes().get(Keys.IS_GENERIC) && testCase.hasGenericCode()) {
            Circuit c = new ResolveGenerics().resolveCircuit(testCase.getVisualElement(), circuit, library).getCircuit();
            model = new ModelCreator(c, library, false).createModel(false);
        } else
            model = new ModelCreator(circuit, library).createModel(false);
        return model;
    }

    /**
     * Use for tests only! Don't use this constructor with a model you have created from a circuit.
     * If a circuit is available use the constructor above.
     *
     * @param testCase the test case
     * @param model    the model
     * @throws TestingDataException TestingDataException
     */
    public TestExecutor(TestCaseDescription testCase, Model model) throws TestingDataException {
        names = testCase.getNames();
        this.model = model;
        results = new ValueTable(names);
        visibleRows = 0;
        lines = testCase.getLines();

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

        context = new Context().setModel(model);

        for (VirtualSignal s : testCase.getVirtualSignals()) {
            final int index = getIndexOf(s.getName());
            if (index >= 0) {
                outputs.add(new TestSignal(index, s.getValue(context)));
                addTo(usedSignals, s.getName());
            }
        }

        for (String name : names)
            if (!usedSignals.contains(name))
                if (allowMissingInputs)
                    inputs.add(new TestSignal(getIndexOf(name), null));
                else
                    throw new TestingDataException(Lang.get("err_testSignal_N_notFound", name));

        if (inputs.size() == 0)
            throw new TestingDataException(Lang.get("err_noTestInputSignalsDefined"));

        if (outputs.size() == 0)
            throw new TestingDataException(Lang.get("err_noTestOutputSignalsDefined"));

        model.init();
        model.addObserver(event -> {
            if (event.getType() == ModelEventType.ERROR_OCCURRED)
                errorOccurred = true;
        }, ModelEventType.ERROR_OCCURRED);
    }

    /**
     * Sets the model to the given row.
     *
     * @param row the row to advance the model to
     * @throws ParserException ParserException
     */
    public void executeTo(int row) throws ParserException {
        execute(new LineListener() {
            private int r = row;

            @Override
            public void add(TestRow testRow) {
                Value[] values = testRow.getValues();
                Value[] res = new Value[values.length];

                if (r >= 0) {
                    advanceModel(model, testRow, values, res);
                    r--;
                }
            }
        }, false);
    }

    /**
     * Creates the result by comparing the testing vector with the given model
     *
     * @return the result of the test execution
     * @throws ParserException      ParserException
     */
    public TestExecutor.Result execute() throws ParserException {
        return execute(values -> checkRow(model, values), true);
    }

    /**
     * Executes the test and sends all the test lines to the {@link LineListener} provided.
     *
     * @param lineListener the line listener to use
     * @param closeModel   if true the model is closed
     * @return the result of the test execution
     * @throws ParserException ParserException
     */
    private TestExecutor.Result execute(LineListener lineListener, boolean closeModel) throws ParserException {
        try {
            lines.emitLines(new LineListenerResolveDontCare(lineListener, inputs), context);
            return new Result();
        } finally {
            if (closeModel)
                model.close();
        }
    }

    private void addTo(HashSet<String> signals, String name) throws TestingDataException {
        if (signals.contains(name))
            throw new TestingDataException(Lang.get("err_nameUsedTwice_N", name));
        signals.add(name);
    }

    private void checkRow(Model model, TestRow testRow) {
        Value[] values = testRow.getValues();
        Value[] res = new Value[values.length];

        advanceModel(model, testRow, values, res);

        boolean ok = true;
        for (TestSignal out : outputs) {
            MatchedValue matchedValue = new MatchedValue(values[out.index], out.value);
            res[out.index] = matchedValue;
            if (!matchedValue.isPassed())
                ok = false;
        }

        if (ok)
            passedCount++;
        else
            failedCount++;

        if (visibleRows < (ok ? MAX_RESULTS : ERR_RESULTS)) {
            visibleRows++;
            results.add(new TestRow(res, testRow.getDescription()).setRow(rowCount));
        } else
            toManyResults = true;
        rowCount++;
    }

    private void advanceModel(Model model, TestRow testRow, Value[] values, Value[] res) {
        boolean clockIsUsed = false;
        // set all values except the clocks
        for (TestSignal in : inputs) {
            if (values[in.index].getType() != Value.Type.CLOCK) {
                if (in.value != null)
                    values[in.index].copyTo(in.value);
            } else {
                clockIsUsed = true;
            }
            res[in.index] = values[in.index];
        }

        try {
            if (clockIsUsed) {  // a clock signal is used
                model.doStep();  // propagate all except clock
                addClockRow(values.length, testRow.getDescription());

                // set clock
                for (TestSignal in : inputs)
                    if (values[in.index].getType() == Value.Type.CLOCK) {
                        values[in.index].copyTo(in.value);
                        res[in.index] = values[in.index];
                    }

                // propagate clock change
                model.doStep();
                addClockRow(values.length, testRow.getDescription());

                // restore clock
                for (TestSignal in : inputs)   // invert the clock values
                    if (values[in.index].getType() == Value.Type.CLOCK) {
                        in.value.setBool(!in.value.getBool());
                        res[in.index] = new Value(in.value);
                    }
            }

            model.doStep();
        } catch (RuntimeException e) {
            errorOccurred = true;
            throw e;
        }
    }

    private void addClockRow(int cols, String description) {
        if (visibleRows < ERR_RESULTS) {
            Value[] r = new Value[cols];
            for (TestSignal out : outputs)
                r[out.index] = new Value(out.value);
            for (TestSignal in : inputs)
                r[in.index] = new Value(in.value);
            results.add(new TestRow(r, description)).omitInTable();
        } else
            toManyResults = true;
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
     * Allow missing inputs
     *
     * @param allowMissingInputs if true, missing inputs are allowed
     * @return this for chained calls
     */
    public TestExecutor setAllowMissingInputs(boolean allowMissingInputs) {
        this.allowMissingInputs = allowMissingInputs;
        return this;
    }

    /**
     * Adds a observer to the model of this test executor
     *
     * @param observer the observer to add
     * @return this for chained calls
     */
    public TestExecutor addObserver(ModelStateObserverTyped observer) {
        model.addObserver(observer);
        return this;
    }

    /**
     * A test signal
     */
    public final static class TestSignal {
        private final int index;
        private final ObservableValue value;

        private TestSignal(int index, ObservableValue value) {
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
     * The result of the test execution
     */
    public final class Result {

        private Result() {
        }

        /**
         * @return true if all tests have passed
         */
        public boolean allPassed() {
            return !errorOccurred && failedCount == 0 && passedCount > 0;
        }

        /**
         * @return true if the test failed due to an error
         */
        public boolean isErrorOccurred() {
            return errorOccurred;
        }

        /**
         * @return the percentage of failed test rows
         */
        public int failedPercent() {
            if (passedCount == 0)
                return 100;
            int p = 100 * failedCount / passedCount;
            if (p == 0 && failedCount > 0)
                p = 1;
            return p;
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

        /**
         * @return the number of rows tested (passed+failed)
         */
        public int getRowsTested() {
            return passedCount + failedCount;
        }

        /**
         * @return the value table containing the detailed result
         */
        public ValueTable getValueTable() {
            return results;
        }
    }
}
