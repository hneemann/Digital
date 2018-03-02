/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.quinemc;

/**
 * Checks if a bool table is independent from a certain variable
 */
public final class IndependentChecker {

    private final BoolTable boolTable;
    private final int vars;
    private final int checkSize;

    /**
     * Creates a new BoolTable
     *
     * @param boolTable the table to investigate
     */
    public IndependentChecker(BoolTable boolTable) {
        this.boolTable = boolTable;

        int v = 0;
        int s = boolTable.size();
        while (s > 1) {
            s = s / 2;
            v++;
        }
        vars = v;
        checkSize = boolTable.size() / 2;
    }

    /**
     * @return the number of variables
     */
    public int getVars() {
        return vars;
    }

    /**
     * checks if the given bool table is dependent on the given variable
     *
     * @param varNum the variable to check
     * @return true if table is independent
     */
    public boolean isIndependentFrom(int varNum) {
        int bitMask = 1 << (vars - varNum - 1);
        int lowMask = bitMask - 1;
        int highMask = ~lowMask;

        for (int n = 0; n < checkSize; n++) {
            int i1 = (n & lowMask) | ((n & highMask) << 1);
            int i2 = i1 | bitMask;

            ThreeStateValue v1 = boolTable.get(i1);
            ThreeStateValue v2 = boolTable.get(i2);

            if (v1.equals(v2))
                continue;

            if (v1.equals(ThreeStateValue.dontCare) || v2.equals(ThreeStateValue.dontCare))
                continue;

            return false;
        }

        return true;
    }

    /**
     * Returns a table with the given variable removed
     *
     * @param varNum the variable to remove
     * @return the reduced BoolTable
     */
    public BoolTable removeVar(int varNum) {
        if (varNum >= vars || varNum < 0)
            throw new RuntimeException("variable does not exist");
        return new BoolTableRemoveVar(boolTable, vars, varNum);
    }

    private static final class BoolTableRemoveVar implements BoolTable {
        private final BoolTable boolTable;
        private final int bitMask;
        private final int lowMask;
        private final int highMask;
        private final int size;

        private BoolTableRemoveVar(BoolTable boolTable, int vars, int varNum) {
            this.boolTable = boolTable;
            bitMask = 1 << (vars - varNum - 1);
            lowMask = bitMask - 1;
            highMask = ~lowMask;
            size = 1 << (vars - 1);
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public ThreeStateValue get(int n) {
            int i = (n & lowMask) | ((n & highMask) << 1);
            ThreeStateValue v = boolTable.get(i);
            if (v.equals(ThreeStateValue.dontCare))
                return boolTable.get(i | bitMask);
            else
                return v;
        }
    }
}
