package de.neemann.digital.gui.components.table;

import de.neemann.digital.analyse.TruthTable;
import de.neemann.digital.analyse.expression.ContextFiller;
import de.neemann.digital.analyse.quinemc.BoolTableByteArray;
import junit.framework.TestCase;

/**
 * @author hneemann
 */
public class TestReorderInputs extends TestCase {

    public void testReorder() throws Exception {
        TruthTable t = new TruthTable(5).addResult();
        BoolTableByteArray col = (BoolTableByteArray) t.getResult(0);
        for (int i = 0; i < t.getRows(); i++)
            col.set(i, i + 1);


        ReorderInputs reorderInputs = new ReorderInputs(t);
        reorderInputs.getItems().swap(1, 2);
        TruthTable newTable = reorderInputs.reorder();


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

        ReorderInputs reorderInputs = new ReorderInputs(t);
        reorderInputs.getItems().delete(2);
        TruthTable newTable = reorderInputs.reorder();

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