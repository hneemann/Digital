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
    private final String label;
    private final ArrayList<String> names;
    private final Model model;
    private final LineEmitter lines;
    private final Context context;
    private final ArrayList<TestSignal> inputs;
    private final ArrayList<TestSignal> outputs;
    private boolean allowMissingInputs;
    private boolean errorOccurred;

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
        this(testCase.getLabel(), testCase.getTestCaseDescription(), createModel(testCase, circuit, library));
    }

    static private Model createModel(Circuit.TestCase testCase, Circuit circuit, ElementLibrary library) throws NodeException, ElementNotFoundException, PinException {
        final Model model;
        if (circuit != null && circuit.getAttributes().get(Keys.IS_GENERIC) && testCase.hasGenericCode()) {
            Circuit c = new ResolveGenerics(circuit, library).resolveCircuit(testCase.getVisualElement().getElementAttributes()).getCircuit();
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
        this("unknown", testCase, model);
    }

    /**
     * Use for tests only! Don't use this constructor with a model you have created from a circuit.
     * If a circuit is available use the constructor above.
     *
     * @param label    the name of this test case
     * @param testCase the test case
     * @param model    the model
     * @throws TestingDataException TestingDataException
     */
    public TestExecutor(String label, TestCaseDescription testCase, Model model) throws TestingDataException {
        this.label = label;
        names = checkForPinNumbers(testCase.getNames(), model);
        this.model = model;
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

        for (Signal s : model.getTestOutputs()) {
            final int index = getIndexOf(s.getName());
            if (index >= 0) {
                outputs.add(new TestSignal(index, s.getValue()));
                addTo(usedSignals, s.getName());
            }
        }

        context = new Context().setModel(model).setSeedReset(testCase::resetSeed);

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

        testCase.getModelInitializer().init(model);

        model.init();
        model.addObserver(event -> {
            if (event.getType() == ModelEventType.ERROR_OCCURRED)
                errorOccurred = true;
        }, ModelEventType.ERROR_OCCURRED);
    }

    private ArrayList<String> checkForPinNumbers(ArrayList<String> names, Model model) {
        ArrayList<String> returnNames = new ArrayList<>();
        for (String n : names)
            if (n.startsWith("/")) {
                String pin = n.substring(1).trim();
                String found = null;
                for (Signal s : model.getSignals()) {
                    if (pin.equals(s.getPinNumber()))
                        found = s.getName();
                }
                if (found == null) {
                    for (Clock s : model.getClocks()) {
                        if (pin.equals(s.getClockPin()))
                            found = s.getLabel();
                    }
                }
                if (found == null)
                    returnNames.add(n);
                else
                    returnNames.add(found);
            } else
                returnNames.add(n);
        return returnNames;
    }

    /**
     * Sets the model to the given row.
     *
     * @param row the row to advance the model to
     * @throws ParserException      ParserException
     * @throws TestingDataException TestingDataException
     */
    public void executeTo(int row) throws ParserException, TestingDataException {
        execute(new TestResultListener() {
            private int r = row;

            @Override
            public void add(TestRow testRow) {
                Value[] values = testRow.getValues();
                Value[] res = new Value[values.length];

                if (r >= 0) {
                    advanceModel(testRow, values, res, this);
                    r--;
                }
            }

            @Override
            public void addClockRow(String description) {
            }

        }, false);
    }

    /**
     * Creates the result by comparing the testing vector with the given model
     *
     * @return the result of the test execution
     * @throws ParserException      ParserException
     * @throws TestingDataException TestingDataException
     */
    public TestResult execute() throws ParserException, TestingDataException {
        return execute(new TestResult(this), true);
    }

    /**
     * Executes the test and sends all the test lines to the {@link LineListener} provided.
     *
     * @param lineListener the line listener to use
     * @param closeModel   if true the model is closed
     * @throws ParserException ParserException
     */
    private <LL extends LineListener> LL execute(LL lineListener, boolean closeModel) throws ParserException, TestingDataException {
        try {
            lines.emitLines(new LineListenerResolveDontCare(lineListener, inputs), context);
            return lineListener;
        } catch (RuntimeException re) {
            errorOccurred = true;
            throw new TestingDataException(Lang.get("err_whileExecutingTests_N0", label), re);
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

    void advanceModel(TestRow testRow, Value[] values, Value[] res, TestResultListener trl) {
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

        if (clockIsUsed) {  // a clock signal is used
            model.doStep();  // propagate all except clock
            trl.addClockRow(testRow.getDescription());

            // set clock
            for (TestSignal in : inputs)
                if (values[in.index].getType() == Value.Type.CLOCK) {
                    values[in.index].copyTo(in.value);
                    res[in.index] = values[in.index];
                }

            // propagate clock change
            model.doStep();
            trl.addClockRow(testRow.getDescription());

            // restore clock
            for (TestSignal in : inputs)   // invert the clock values
                if (values[in.index].getType() == Value.Type.CLOCK) {
                    in.value.setBool(!in.value.getBool());
                    res[in.index] = new Value(in.value);
                }
        }

        model.doStep();
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
     * @return the list of outputs
     */
    public ArrayList<TestSignal> getOutputs() {
        return outputs;
    }

    /**
     * @return the list of inputs
     */
    public ArrayList<TestSignal> getInputs() {
        return inputs;
    }

    /**
     * @return true if an error has occurred
     */
    public boolean isErrorOccurred() {
        return errorOccurred;
    }

    /**
     * @return the list of names in the test header
     */
    public ArrayList<String> getNames() {
        return names;
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

        /**
         * @return the observable value of this index
         */
        public ObservableValue getValue() {
            return value;
        }
    }
}
