package de.neemann.digital.testing;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.Signal;
import de.neemann.digital.core.wiring.Clock;
import de.neemann.digital.lang.Lang;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

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
    private Exception exception;

    /**
     * Creates a new testing result
     *
     * @param testData the testing data
     * @throws TestingDataException DataException
     */
    public TestResult(TestData testData) throws TestingDataException {
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
     * @throws NodeException        NodeException
     */
    public TestResult create(Model model) throws TestingDataException, NodeException {
        allPassed = true;
        HashSet<String> usedSignals = new HashSet<>();

        ArrayList<TestSignal> inputs = new ArrayList<>();
        for (Signal s : model.getInputs()) {
            final int index = getIndexOf(s.getName());
            if (index >= 0) {
                inputs.add(new TestSignal(index, s.getValue()));
                usedSignals.add(s.getName());
            }
        }
        for (Clock c : model.getClocks()) {
            final int index = getIndexOf(c.getLabel());
            if (index >= 0) {
                inputs.add(new TestSignal(index, c.getClockOutput()));
                usedSignals.add(c.getLabel());
            }
        }

        ArrayList<TestSignal> outputs = new ArrayList<>();
        for (Signal s : model.getOutputs()) {
            final int index = getIndexOf(s.getName());
            if (index >= 0) {
                outputs.add(new TestSignal(index, s.getValue()));
                usedSignals.add(s.getName());
            }
        }

        if (inputs.size() == 0)
            throw new TestingDataException(Lang.get("err_noTestInputSignalsDefined"));

        if (outputs.size() == 0)
            throw new TestingDataException(Lang.get("err_noTestOutputSignalsDefined"));

        for (String name : names)
            if (!usedSignals.contains(name))
                throw new TestingDataException(Lang.get("err_testSignal_N_notFound", name));

        model.init();

        for (Value[] rowWithDontCare : lines) {

            for (Value[] row : resolveDontCares(inputs, rowWithDontCare)) {

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

                try {
                    model.doStep();
                } catch (NodeException | RuntimeException e) {
                    exception = e;
                    allPassed = false;
                    return this;
                }

                for (TestSignal out : outputs) {
                    MatchedValue matchedValue = new MatchedValue(row[out.index], out.value);
                    res[out.index] = matchedValue;
                    if (!matchedValue.isPassed())
                        allPassed = false;
                }
                results.add(res);
            }
        }

        return this;
    }

    private Iterable<Value[]> resolveDontCares(ArrayList<TestSignal> inputs, Value[] rowWithDontCare) {
        ArrayList<Integer> dcIndex = null;
        for (TestSignal in : inputs) {
            if (rowWithDontCare[in.index].getType() == Value.Type.DONTCARE) {
                if (dcIndex == null)
                    dcIndex = new ArrayList<>();
                dcIndex.add(in.index);
            }
        }
        if (dcIndex == null)
            return new SingleItemIterator<>(rowWithDontCare);
        else {
            return new VariantsIterator(dcIndex, rowWithDontCare);
        }
    }

    /**
     * @return true if all tests have passed
     */
    public boolean allPassed() {
        return allPassed;
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

    private static class SingleItemIterator<T> implements Iterable<T> {
        private final T value;

        SingleItemIterator(T value) {
            this.value = value;
        }

        @Override
        public Iterator<T> iterator() {
            return new SingleItemIterable<>(value);
        }
    }

    private static class SingleItemIterable<T> implements Iterator<T> {
        private T value;

        SingleItemIterable(T value) {
            this.value = value;
        }

        @Override
        public boolean hasNext() {
            return value != null;
        }

        @Override
        public T next() {
            if (value == null)
                throw new NoSuchElementException();
            T r = value;
            value = null;
            return r;
        }
    }

    private static class VariantsIterator implements Iterable<Value[]> {
        private final ArrayList<Integer> dcIndex;
        private final Value[] rowWithDontCare;

        VariantsIterator(ArrayList<Integer> dcIndex, Value[] rowWithDontCare) {
            this.dcIndex = dcIndex;
            this.rowWithDontCare = rowWithDontCare;
        }

        @Override
        public Iterator<Value[]> iterator() {
            Value[] copy = new Value[rowWithDontCare.length];
            for (int i = 0; i < copy.length; i++)
                copy[i] = new Value(rowWithDontCare[i]);
            return new VariantsIterable(dcIndex, copy);
        }
    }

    private static class VariantsIterable implements Iterator<Value[]> {
        private final ArrayList<Integer> dcIndex;
        private final Value[] row;
        private final int count;
        private int n;

        VariantsIterable(ArrayList<Integer> dcIndex, Value[] row) {
            this.dcIndex = dcIndex;
            this.row = row;
            count = 1 << dcIndex.size();
            n = 0;
        }

        @Override
        public boolean hasNext() {
            return n < count;
        }

        @Override
        public Value[] next() {
            if (n >= count)
                throw new NoSuchElementException();
            int mask = 1;
            for (int in : dcIndex) {
                boolean val = (n & mask) != 0;
                row[in] = new Value(val ? 1 : 0);
                mask *= 2;
            }
            n++;
            return row;
        }
    }

    /**
     * @return the exception thrown during test test execution, or null if there was no error.
     */
    public Exception getException() {
        return exception;
    }
}
