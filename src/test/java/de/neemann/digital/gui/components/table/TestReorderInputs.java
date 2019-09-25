/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.table;

import de.neemann.digital.analyse.TruthTable;
import de.neemann.digital.analyse.expression.ContextFiller;
import de.neemann.digital.analyse.quinemc.BoolTableByteArray;
import junit.framework.TestCase;

/**
 */
public class TestReorderInputs extends TestCase {

    public void testReorder() throws Exception {
        TruthTable t = new TruthTable(5).addResult();
        BoolTableByteArray col = (BoolTableByteArray) t.getResult(0);
        for (int i = 0; i < t.getRows(); i++)
            col.set(i, i + 1);


        TruthTable newTable = t.createDeepCopy();
        ReorderInputs reorderInputs = new ReorderInputs(newTable);
        reorderInputs.getItems().swap(1, 2);
        reorderInputs.reorder();


        ContextFiller cf = new ContextFiller(t.getVars());
        for (int i = 0; i < t.getRows(); i++) {
            cf.setContextTo(i);
            assertEquals(newTable.getByContext(0, cf), t.getByContext(0, cf));
        }
    }

    public void testDeleteVar() throws Exception {
        TruthTable t = new TruthTable(3).addResult();
        BoolTableByteArray col = (BoolTableByteArray) t.getResult(0);
        for (int i = 0; i < t.getRows(); i++)
            col.set(i, i + 1);

        TruthTable newTable = t.createDeepCopy();
        ReorderInputs reorderInputs = new ReorderInputs(newTable);
        reorderInputs.getItems().delete(2);
        reorderInputs.reorder();

        assertEquals(2, newTable.getVars().size());
        assertEquals(1, newTable.getResultCount());

        ContextFiller cf = new ContextFiller(newTable.getVars());
        cf.set(t.getVars().get(2), false);
        for (int i = 0; i < newTable.getRows(); i++) {
            cf.setContextTo(i);
            assertEquals(newTable.getByContext(0, cf), t.getByContext(0, cf));
        }
    }

}
