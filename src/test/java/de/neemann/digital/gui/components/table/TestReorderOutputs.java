package de.neemann.digital.gui.components.table;

import de.neemann.digital.analyse.TruthTable;
import de.neemann.digital.analyse.quinemc.BoolTableByteArray;
import junit.framework.TestCase;

/**
 * @author hneemann
 */
public class TestReorderOutputs extends TestCase {

    public void testDeleteResult() throws Exception {
        TruthTable t = new TruthTable(3).addResult().addResult();
        BoolTableByteArray col = (BoolTableByteArray) t.getResult(0);
        for (int i = 0; i < t.getRows(); i++)
            col.set(i, i + 1);

        ReorderOutputs reorderOutputs = new ReorderOutputs(t);
        reorderOutputs.getItems().delete(1);
        TruthTable newTable = reorderOutputs.reorder();

        assertEquals(3, newTable.getVars().size());
        assertEquals(1, newTable.getResultCount());
    }

}