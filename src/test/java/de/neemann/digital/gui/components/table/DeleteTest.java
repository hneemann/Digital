package de.neemann.digital.gui.components.table;

import de.neemann.digital.analyse.TruthTable;
import de.neemann.digital.analyse.expression.ContextFiller;
import de.neemann.digital.analyse.quinemc.BoolTableIntArray;
import junit.framework.TestCase;

/**
 * @author hneemann
 */
public class DeleteTest extends TestCase {

    public void testDeleteVar() throws Exception {
        TruthTable t = new TruthTable(3).addResult();
        BoolTableIntArray col = (BoolTableIntArray) t.getResult(0);
        for (int i = 0; i < t.getRows(); i++)
            col.set(i, i + 1);

        TruthTable newTable = new Delete(t).delete(1);

        assertEquals(2, newTable.getVars().size());
        assertEquals(1, newTable.getResultCount());

        ContextFiller cf = new ContextFiller(newTable.getVars());
        cf.set(t.getVars().get(1), false);
        for (int i = 0; i < newTable.getRows(); i++) {
            cf.setContextTo(i);
            assertEquals(newTable.getByContext(0, cf), t.getByContext(0, cf));
        }
    }

    public void testDeleteResult() throws Exception {
        TruthTable t = new TruthTable(3).addResult().addResult();
        BoolTableIntArray col = (BoolTableIntArray) t.getResult(0);
        for (int i = 0; i < t.getRows(); i++)
            col.set(i, i + 1);

        TruthTable newTable = new Delete(t).delete(3);

        assertEquals(3, newTable.getVars().size());
        assertEquals(1, newTable.getResultCount());
    }

}