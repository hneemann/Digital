/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.testing;

import de.neemann.digital.data.Value;
import de.neemann.digital.data.ValueTable;
import de.neemann.digital.testing.parser.TestRow;

/**
 * The test result created by the test executor
 */
public class TestResult implements TestResultListener {
    private static final int MAX_RESULTS = 1 << 10;
    private static final int ERR_RESULTS = MAX_RESULTS * 2;

    private final TestExecutor testExecutor;
    private final ValueTable results;
    private int passedCount;
    private int failedCount;
    private boolean toManyResults;
    private int visibleRows;
    private int rowCount;

    /**
     * Creates a new instance
     *
     * @param testExecutor the test executor that created tis instance
     */
    TestResult(TestExecutor testExecutor) {
        this.testExecutor = testExecutor;
        results = new ValueTable(testExecutor.getNames());
    }

    @Override
    public void add(TestRow testRow) {
        Value[] values = testRow.getValues();
        Value[] res = new Value[values.length];

        testExecutor.advanceModel(testRow, values, res, this);

        boolean ok = true;
        for (TestExecutor.TestSignal out : testExecutor.getOutputs()) {
            MatchedValue matchedValue = new MatchedValue(values[out.getIndex()], out.getValue());
            res[out.getIndex()] = matchedValue;
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

    @Override
    public void addClockRow(String description) {
        if (visibleRows < ERR_RESULTS) {
            Value[] r = new Value[testExecutor.getNames().size()];
            for (TestExecutor.TestSignal out : testExecutor.getOutputs())
                r[out.getIndex()] = new Value(out.getValue());
            for (TestExecutor.TestSignal in : testExecutor.getInputs())
                r[in.getIndex()] = new Value(in.getValue());
            results.add(new TestRow(r, description)).omitInTable();
        } else
            toManyResults = true;
    }


    /**
     * @return true if all tests have passed
     */
    public boolean allPassed() {
        return !isErrorOccurred() && failedCount == 0 && passedCount > 0;
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
     * @return true if the test failed due to an error
     */
    public boolean isErrorOccurred() {
        return testExecutor.isErrorOccurred();
    }

    /**
     * @return the percentage of failed test rows
     */
    public int failedPercent() {
        if (passedCount == 0)
            return 100;
        int p = (100 * failedCount) / (failedCount + passedCount);
        if (p == 0 && failedCount > 0)
            p = 1;
        return p;
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
