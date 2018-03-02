/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse;

import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.analyse.quinemc.BoolTable;
import de.neemann.digital.analyse.quinemc.BoolTableByteArray;
import de.neemann.digital.analyse.quinemc.ThreeStateValue;
import de.neemann.digital.core.Model;
import de.neemann.digital.integration.ToBreakRunner;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import static de.neemann.digital.analyse.quinemc.ThreeStateValue.one;
import static de.neemann.digital.analyse.quinemc.ThreeStateValue.zero;

/**
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
        checkTable(tt.getResult("Q0n+1"), one, zero, one, zero);
        checkTable(tt.getResult("Q1n+1"), zero, one, one, zero);

        assertEquals("Y1", tt.getResultName(2));
        assertEquals("Y0", tt.getResultName(3));
        final BoolTable y1 = tt.getResult(2);
        final BoolTable y0 = tt.getResult(3);
        for (int i = 0; i < 4; i++) {
            assertEquals((i & 1) > 0, y0.get(i).invert().bool());
            assertEquals((i & 2) > 0, y1.get(i).invert().bool());
        }
    }


    public void testAnalyzerMultiBit2() throws Exception {
        Model model = new ToBreakRunner("dig/analyze/multiBitInOut.dig", false).getModel();
        TruthTable tt = new ModelAnalyser(model).analyse();
        checkIdent(tt);

        TreeMap<String, String> p = tt.getModelAnalyzerInfo().getPins();
        assertEquals("i1", p.get("A0"));
        assertEquals("i2", p.get("A1"));
        assertEquals("o1", p.get("B0"));
        assertEquals("o2", p.get("B1"));
    }

    // test with non zero default values set
    public void testAnalyzerMultiBit3() throws Exception {
        Model model = new ToBreakRunner("dig/analyze/multiBitInOutDef.dig", false).getModel();
        TruthTable tt = new ModelAnalyser(model).analyse();
        checkIdent(tt);
    }

    private void checkIdent(TruthTable tt) {
        checkTable(tt.getResult("B1"), zero, zero, one, one);
        checkTable(tt.getResult("B0"), zero, one, zero, one);
    }

    private void checkTable(BoolTable table, ThreeStateValue... expected) {
        assertNotNull("result not found", table);
        assertEquals("wrong table size", expected.length, table.size());
        for (int i = 0; i < expected.length; i++)
            assertEquals("wrong value " + i, expected[i], table.get(i));
    }


    public void testAnalyzerBacktrack() throws Exception {
        Model model = new ToBreakRunner("dig/analyze/analyzeBacktrack.dig", false).getModel();
        TruthTable tt = new ModelAnalyser(model).analyse();

        final BoolTable Y1 = tt.getResult("1Y");
        checkRemaining(Y1, "1A", "1B");
        checkTable(getInner(Y1), zero, one, one, zero);

        final BoolTable Y2 = tt.getResult("2Y");
        checkRemaining(Y2, "2A", "2B");
        checkTable(getInner(Y2), one, zero, zero, one);

        final BoolTable Y3 = tt.getResult("3Y");
        checkRemaining(Y3, "3A", "3B");
        checkTable(getInner(Y3), zero, one, one, one);

        final BoolTable Y4 = tt.getResult("4Y");
        checkRemaining(Y4, "4A", "4B", "4C");
        checkTable(getInner(Y4), zero, zero, zero, zero, zero, zero, zero, one);
    }

    private BoolTableByteArray getInner(BoolTable table) {
        assertTrue(table instanceof BoolTableExpanded);
        return ((BoolTableExpanded) table).getBoolTable();
    }

    private void checkRemaining(BoolTable table, String... vars) {
        assertTrue(table instanceof BoolTableExpanded);
        ArrayList<Variable> v = ((BoolTableExpanded) table).getVars();
        assertEquals(vars.length, v.size());
        for (int i = 0; i < vars.length; i++)
            assertEquals(vars[i], v.get(i).getIdentifier());
    }

    public void testAnalyzerMultiBitPins() throws Exception {
        Model model = new ToBreakRunner("dig/analyze/multiBitInOutXOr.dig", false).getModel();
        ModelAnalyserInfo mai = new ModelAnalyser(model).analyse().getModelAnalyzerInfo();

        assertEquals(2, mai.getInputBusMap().size());
        checkBus(mai.getInputBusMap(), "A", "A0", "A1", "A2", "A3");
        checkBus(mai.getInputBusMap(), "B", "B0", "B1", "B2", "B3");

        assertEquals(1, mai.getOutputBusMap().size());
        checkBus(mai.getOutputBusMap(), "S", "S0", "S1", "S2", "S3");
    }

    private void checkBus(HashMap<String, ArrayList<String>> busMap, String name, String... names) {
        ArrayList<String> n = busMap.get(name);
        assertNotNull(n);
        assertEquals(names.length, n.size());
        for (int i = 0; i < names.length; i++)
            assertEquals(names[i], n.get(i));
    }

}
