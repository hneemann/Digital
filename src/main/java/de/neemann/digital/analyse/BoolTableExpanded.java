/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse;

import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.analyse.quinemc.BoolTable;
import de.neemann.digital.analyse.quinemc.BoolTableByteArray;
import de.neemann.digital.analyse.quinemc.ThreeStateValue;
import de.neemann.digital.core.Bits;
import de.neemann.digital.core.Signal;

import java.util.ArrayList;

/**
 * Creates a bool table which represents an expression which does not depend on all variables.
 */
public class BoolTableExpanded implements BoolTable {
    private final BoolTableByteArray e;
    private final ArrayList<Variable> vars;
    private final int[] bitsToRemove;
    private final int bitRemoveCount;
    private final int size;

    /**
     * Creates a new instance
     *
     * @param e              the values
     * @param inputs         the variables the expression relay depends on
     * @param originalInputs all variables
     */
    public BoolTableExpanded(BoolTableByteArray e, ArrayList<Signal> inputs, ArrayList<Signal> originalInputs) {
        this.e = e;
        bitRemoveCount = originalInputs.size() - inputs.size();
        bitsToRemove = new int[bitRemoveCount];
        size = 1 << originalInputs.size();

        int bit = 0;
        int c = 0;
        for (int i = originalInputs.size() - 1; i >= 0; i--) {
            Signal s = originalInputs.get(i);
            if (!inputs.contains(s)) {
                bitsToRemove[c] = bit;
                c++;
            }
            bit++;
        }

        vars = new ArrayList<>();
        for (Signal s : inputs)
            vars.add(new Variable(s.getName()));
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public ThreeStateValue get(int i) {
        for (int b = bitRemoveCount - 1; b >= 0; b--)
            i = Bits.removeBitFromValue(i, bitsToRemove[b]);
        return e.get(i);
    }

    /**
     * @return the bool table
     */
    public BoolTableByteArray getBoolTable() {
        return e;
    }

    /**
     * @return the variables
     */
    public ArrayList<Variable> getVars() {
        return vars;
    }

    @Override
    public int realSize() {
        return e.realSize();
    }
}
