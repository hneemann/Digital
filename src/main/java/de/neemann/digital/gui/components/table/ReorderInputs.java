/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.table;

import de.neemann.digital.analyse.TruthTable;
import de.neemann.digital.analyse.expression.ContextFiller;
import de.neemann.digital.analyse.expression.ExpressionException;
import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.gui.components.ElementOrderer;
import de.neemann.digital.lang.Lang;

import java.util.ArrayList;

/**
 * Used to reorder the table inputs
 */
public class ReorderInputs {
    private final TruthTable table;
    private final ArrayList<String> names;

    /**
     * /**
     * Creates a new instance
     *
     * @param table the original table
     * @param names the new ordering
     */
    public ReorderInputs(TruthTable table, ArrayList<String> names) {
        this.table = table;
        this.names = names;
    }

    /**
     * Creates a new instance
     *
     * @param table the original table
     */
    public ReorderInputs(TruthTable table) {
        this.table = table;
        names = new ArrayList<>();
        for (Variable v : table.getVars())
            names.add(v.getIdentifier());
    }

    /**
     * @return the items to reorder
     */
    public ElementOrderer.OrderInterface<String> getItems() {
        return new ElementOrderer.ListOrder<>(names);
    }

    /**
     * Creates a new table matching the actual state of the items
     *
     * @return the new table
     * @throws ExpressionException ExpressionException
     */
    public TruthTable reorder() throws ExpressionException {
        ArrayList<Variable> newVars = new ArrayList<>();
        ArrayList<Variable> deletedVars = new ArrayList<>(table.getVars());

        for (String name : names) {
            Variable found = null;
            for (Variable v : deletedVars)
                if (v.getIdentifier().equals(name)) {
                    found = v;
                    break;
                }

            if (found != null) {
                newVars.add(found);
                deletedVars.remove(found);
            }
        }

        if (newVars.size() < 2)
            throw new ExpressionException(Lang.get("err_tableBecomesToSmall"));

        TruthTable newTable = new TruthTable(newVars);
        newTable.setModelAnalyzerInfo(table.getModelAnalyzerInfo());
        for (int j = 0; j < table.getResultCount(); j++)
            newTable.addResult(table.getResultName(j));

        ContextFiller fc = new ContextFiller(newTable.getVars());
        for (Variable v : deletedVars)
            fc.set(v, false);

        for (int row = 0; row < newTable.getRows(); row++) {
            fc.setContextTo(row);
            for (int t = 0; t < newTable.getResultCount(); t++)
                newTable.setByContext(t, fc, table.getByContext(t, fc));
        }

        return newTable;
    }
}
