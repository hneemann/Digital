/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.quinemc;

import de.neemann.digital.analyse.BoolTableExpanded;
import de.neemann.digital.analyse.expression.Variable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * If the result does not depend on a certain variable, this variable is removed.
 * <p>
 */
public class TableReducer {

    private List<Variable> vars;
    private BoolTable table;

    /**
     * Creates a new instance
     *
     * @param vars  the variable
     * @param table the bool table
     */
    public TableReducer(List<Variable> vars, BoolTable table) {
        this.vars = new ArrayList<>(vars);
        this.table = table;
    }

    /**
     * Returns true if it is possible to reduce variables
     *
     * @return true is reduction was possible
     */
    public boolean canReduce() {
        if (table instanceof BoolTableExpanded) {
            BoolTableExpanded t = (BoolTableExpanded) table;
            vars = new ArrayList<>(t.getVars());
            table = t.getBoolTable();
            canReduceOnlyCheckTable();
            return true;
        } else
            return canReduceOnlyCheckTable();
    }

    /**
     * Returns true if it is possible to reduce variables
     * Only used for tests!!!
     *
     * @return true is reduction was possible
     */
    public boolean canReduceOnlyCheckTable() {
        boolean isReduced = false;

        Iterator<Variable> it = vars.iterator();
        int var = 0;
        while (it.hasNext()) {
            it.next();
            IndependentChecker ic = new IndependentChecker(table);
            if (ic.isIndependentFrom(var)) {
                it.remove();
                table = ic.removeVar(var);
                isReduced = true;
            } else {
                var++;
            }
        }
        return isReduced;
    }

    /**
     * @return the remaining variables
     */
    public List<Variable> getVars() {
        return vars;
    }

    /**
     * @return the reduced table
     */
    public BoolTable getTable() {
        return table;
    }
}
