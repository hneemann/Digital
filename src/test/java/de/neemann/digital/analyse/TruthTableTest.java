package de.neemann.digital.analyse;

import de.neemann.digital.analyse.expression.ContextFiller;
import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.analyse.quinemc.BoolTableIntArray;
import junit.framework.TestCase;

import java.util.ArrayList;

/**
 * @author hneemann
 */
public class TruthTableTest extends TestCase {

    public void testGetRows() throws Exception {
        TruthTable t = new TruthTable(3);

        assertEquals(8, t.getRows());
    }

    public void testGetByContext() throws Exception {
        ArrayList<Variable> vars = Variable.vars(5);
        TruthTable t = new TruthTable(vars).addResult();
        BoolTableIntArray result = (BoolTableIntArray) t.getResult(0);
        for (int i = 0; i < t.getRows(); i++) {
            result.set(i, i % 3);
        }

        ContextFiller fc = new ContextFiller(vars);
        for (int i = 0; i < t.getRows(); i++) {
            fc.setContextTo(i);
            assertEquals(i % 3, t.getByContext(0, fc));
        }
    }
}