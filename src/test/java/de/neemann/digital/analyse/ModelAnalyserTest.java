package de.neemann.digital.analyse;

import de.neemann.digital.analyse.quinemc.BoolTable;
import de.neemann.digital.core.Model;
import de.neemann.digital.integration.ToBreakRunner;
import junit.framework.TestCase;

import static de.neemann.digital.analyse.quinemc.ThreeStateValue.one;
import static de.neemann.digital.analyse.quinemc.ThreeStateValue.zero;

/**
 * @author hneemann
 */
public class ModelAnalyserTest extends TestCase {

    public void testAnalyzer() throws Exception {
        Model model = new ToBreakRunner("dig/analyze/analyzeTest.dig").getModel();
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

    public void testAnalyzerDFF() throws Exception {
        Model model = new ToBreakRunner("dig/analyze/analyzeTestDFF.dig").getModel();
        TruthTable tt = new ModelAnalyser(model).analyse();
        check2BitCounter(tt);
    }

    public void testAnalyzerJKFF() throws Exception {
        Model model = new ToBreakRunner("dig/analyze/analyzeTestJKFF.dig", false).getModel();
        TruthTable tt = new ModelAnalyser(model).analyse();
        check2BitCounter(tt);
    }

    public void testAnalyzerTFF() throws Exception {
        Model model = new ToBreakRunner("dig/analyze/analyzeTestTFF.dig", false).getModel();
        TruthTable tt = new ModelAnalyser(model).analyse();
        check2BitCounter(tt);
    }

    public void testAnalyzerTFFEnable() throws Exception {
        Model model = new ToBreakRunner("dig/analyze/analyzeTestTFFEnable.dig", false).getModel();
        TruthTable tt = new ModelAnalyser(model).analyse();
        check2BitCounter(tt);
    }

    private void check2BitCounter(TruthTable tt) {
        assertEquals(4, tt.getRows());
        assertEquals(4, tt.getCols());

        // first col is XOr:
        assertEquals(0, tt.getValue(0, 2));
        assertEquals(1, tt.getValue(1, 2));
        assertEquals(1, tt.getValue(2, 2));
        assertEquals(0, tt.getValue(3, 2));

        // second col
        assertEquals(1, tt.getValue(0, 3));
        assertEquals(0, tt.getValue(1, 3));
        assertEquals(1, tt.getValue(2, 3));
        assertEquals(0, tt.getValue(3, 3));

        assertEquals("Q_1n\tQ_0n\tQ_1n+1\tQ_0n+1\t\n" +
                "0\t0\t0\t1\t\n" +
                "0\t1\t1\t0\t\n" +
                "1\t0\t1\t1\t\n" +
                "1\t1\t0\t0\t\n", tt.toString());
    }

    public void testAnalyzerUniqueNames() throws Exception {
        Model model = new ToBreakRunner("dig/analyze/uniqueNames.dig", false).getModel();
        try {
            new ModelAnalyser(model);
            fail();
        } catch (AnalyseException e) {

        }
    }

    public void testAnalyzerUniqueNames2() throws Exception {
        Model model = new ToBreakRunner("dig/analyze/uniqueNames2.dig", false).getModel();
        try {
            new ModelAnalyser(model);
            fail();
        } catch (AnalyseException e) {

        }
    }

    public void testAnalyzerMultiBit() throws Exception {
        Model model = new ToBreakRunner("dig/analyze/multiBitCounter.dig", false).getModel();
        TruthTable tt = new ModelAnalyser(model).analyse();
        assertEquals("Q0n+1", tt.getResultName(1));
        final BoolTable r0 = tt.getResult(1);
        assertEquals(4, r0.size());
        assertEquals(one, r0.get(0));
        assertEquals(zero, r0.get(1));
        assertEquals(one, r0.get(2));
        assertEquals(zero, r0.get(3));

        assertEquals("Q1n+1", tt.getResultName(0));
        final BoolTable r1 = tt.getResult(0);
        assertEquals(4, r1.size());
        assertEquals(zero, r1.get(0));
        assertEquals(one, r1.get(1));
        assertEquals(one, r1.get(2));
        assertEquals(zero, r1.get(3));

        assertEquals("Y0", tt.getResultName(2));
        assertEquals("Y1", tt.getResultName(3));
        final BoolTable y0 = tt.getResult(2);
        final BoolTable y1 = tt.getResult(3);
        for (int i = 0; i < 4; i++) {
            assertEquals((i & 1) > 0, y0.get(i).invert().bool());
            assertEquals((i & 2) > 0, y1.get(i).invert().bool());
        }
    }


    public void testAnalyzerMultiBit2() throws Exception {
        Model model = new ToBreakRunner("dig/analyze/multiBitInOut.dig", false).getModel();
        TruthTable tt = new ModelAnalyser(model).analyse();
        checkIdent(tt);
    }

    // test with non zero default values set
    public void testAnalyzerMultiBit3() throws Exception {
        Model model = new ToBreakRunner("dig/analyze/multiBitInOutDef.dig", false).getModel();
        TruthTable tt = new ModelAnalyser(model).analyse();
        checkIdent(tt);
    }

    private void checkIdent(TruthTable tt) {
        assertEquals("B0", tt.getResultName(0));
        final BoolTable r0 = tt.getResult(0);
        assertEquals(4, r0.size());
        assertEquals(zero, r0.get(0));
        assertEquals(zero, r0.get(1));
        assertEquals(one, r0.get(2));
        assertEquals(one, r0.get(3));

        assertEquals("B1", tt.getResultName(1));
        final BoolTable r1 = tt.getResult(1);
        assertEquals(4, r1.size());
        assertEquals(zero, r1.get(0));
        assertEquals(one, r1.get(1));
        assertEquals(zero, r1.get(2));
        assertEquals(one, r1.get(3));
    }

}