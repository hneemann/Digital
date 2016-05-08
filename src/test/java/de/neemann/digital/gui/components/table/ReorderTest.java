package de.neemann.digital.gui.components.table;

import de.neemann.digital.analyse.TruthTable;
import de.neemann.digital.analyse.expression.ContextFiller;
import de.neemann.digital.analyse.quinemc.BoolTableIntArray;
import junit.framework.TestCase;

/**
 * @author hneemann
 */
public class ReorderTest extends TestCase {

    public void testReorder() throws Exception {
        TruthTable t = new TruthTable(5).addResult();
        BoolTableIntArray col = (BoolTableIntArray) t.getResult(0);
        for (int i = 0; i < t.getRows(); i++)
            col.set(i, i + 1);


        int[] swap = new int[]{4, 3, 2, 0, 1};
        TruthTable newTable = new Reorder(t).reorder(swap);


        ContextFiller cf = new ContextFiller(t.getVars());
        for (int i = 0; i < t.getRows(); i++) {
            cf.setContextTo(i);
            assertEquals(newTable.getByContext(0, cf), t.getByContext(0, cf));
        }
    }

}