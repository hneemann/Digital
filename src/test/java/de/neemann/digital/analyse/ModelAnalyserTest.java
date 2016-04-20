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
        ModelAnalyser ma = new ModelAnalyser(model).analyse();

        assertEquals(4, ma.getRows());
        assertEquals(3, ma.getCols());

        // circuit is XOr:
        assertEquals(0, ma.getValue(0, 2));
        assertEquals(1, ma.getValue(1, 2));
        assertEquals(1, ma.getValue(2, 2));
        assertEquals(0, ma.getValue(3, 2));

        assertEquals("A\tB\tY\t\n" +
                "0\t0\t0\t\n" +
                "0\t1\t1\t\n" +
                "1\t0\t1\t\n" +
                "1\t1\t0\t\n", ma.toString());
    }
}