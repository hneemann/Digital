package de.neemann.digital.analyse;

import de.neemann.digital.core.Model;
import de.neemann.digital.integration.ToBreakRunner;
import junit.framework.TestCase;

/**
 * @author hneemann
 */
public class ModelAnalyserTest extends TestCase {

    public void testAnalyzer() throws Exception {
        Model model = new ToBreakRunner("dig/analyzeTest.dig").getModel();
        TruthTable tt = new ModelAnalyser(model).analyse();

        assertEquals(4, tt.getRows());
        assertEquals(3, tt.getCols());

        // circuit is XOr:
        assertEquals(0, tt.getValue(0, 2));
        assertEquals(1, tt.getValue(1, 2));
        assertEquals(1, tt.getValue(2, 2));
        assertEquals(0, tt.getValue(3, 2));

        assertEquals("A\tB\tY\t\n" +
                "0\t0\t0\t\n" +
                "0\t1\t1\t\n" +
                "1\t0\t1\t\n" +
                "1\t1\t0\t\n", tt.toString());
    }
}