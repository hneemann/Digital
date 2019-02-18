/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.testing;

import de.neemann.digital.data.Value;
import de.neemann.digital.testing.parser.LineListener;
import de.neemann.digital.testing.parser.TestRow;

import java.util.ArrayList;

/**
 * Resolves don't cares in the inputs list
 */
public class LineListenerResolveDontCare implements LineListener {

    private final LineListener parent;
    private final ArrayList<TestExecutor.TestSignal> inputs;

    /**
     * Create a new instance
     *
     * @param parent the parent listener
     * @param inputs the input test signals
     */
    public LineListenerResolveDontCare(LineListener parent, ArrayList<TestExecutor.TestSignal> inputs) {
        this.parent = parent;
        this.inputs = inputs;
    }

    @Override
    public void add(TestRow testRow) {
        Value[] row = testRow.getValues();
        ArrayList<Integer> dcIndex = null;
        for (TestExecutor.TestSignal in : inputs) {
            if (row[in.getIndex()].getType() == Value.Type.DONTCARE) {
                if (dcIndex == null)
                    dcIndex = new ArrayList<>();
                dcIndex.add(in.getIndex());
            }
        }
        if (dcIndex == null)
            parent.add(testRow);
        else {
            int count = 1 << dcIndex.size();
            for (int n = 0; n < count; n++) {
                int mask = 1;
                for (int in : dcIndex) {
                    boolean val = (n & mask) != 0;
                    row[in] = new Value(val ? 1 : 0);
                    mask *= 2;
                }
                parent.add(new TestRow(row, testRow.getDescription() + ";X=" + n));
            }
        }
    }
}
