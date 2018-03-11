/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.table;

import de.neemann.digital.analyse.TruthTable;
import de.neemann.digital.analyse.expression.ExpressionException;
import de.neemann.digital.gui.components.ElementOrderer;
import de.neemann.digital.lang.Lang;

import java.util.ArrayList;

/**
 * Used to reorder the table outputs
 */
public class ReorderOutputs {
    private final TruthTable table;
    private final ArrayList<String> names;


    /**
     * Creates a new instance
     *
     * @param table the original table
     * @param names the new ordering
     */
    public ReorderOutputs(TruthTable table, ArrayList<String> names) {
        this.table = table;
        this.names = names;
    }

    /**
     * Creates a new instance
     *
     * @param table the original table
     */
    public ReorderOutputs(TruthTable table) {
        this.table = table;
        names = new ArrayList<>();
        for (int i = 0; i < table.getResultCount(); i++)
            names.add(table.getResultName(i));
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
        TruthTable newTable = new TruthTable(table.getVars());
        for (String name : names) {
            for (int i = 0; i < table.getResultCount(); i++)
                if (table.getResultName(i).equals(name)) {
                    newTable.addResult(table.getResultName(i), table.getResult(i));
                    break;
                }
        }

        if (newTable.getResultCount() < 1)
            throw new ExpressionException(Lang.get("err_oneResultIsRequired"));

        return newTable;
    }
}
