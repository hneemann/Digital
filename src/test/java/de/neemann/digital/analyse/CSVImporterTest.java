/*
 * Copyright (c) 2021 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse;

import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.analyse.quinemc.ThreeStateValue;
import de.neemann.digital.gui.components.table.ExpressionListenerCSVCondensed;
import junit.framework.TestCase;

import java.io.IOException;
import java.util.ArrayList;

public class CSVImporterTest extends TestCase {

    public void testSimple() throws IOException {
        TruthTable tt = CSVImporter.readCSV("A,B,,Y\n0,0,,0\n0,1,,0\n1,0,,0\n1,1,,1");
        assertNotNull(tt);

        ArrayList<Variable> vars = tt.getVars();
        assertEquals(2, vars.size());
        assertEquals("A", vars.get(0).getIdentifier());
        assertEquals("B", vars.get(1).getIdentifier());
        assertEquals(1, tt.getResultCount());
        assertEquals("Y", tt.getResultName(0));

        assertEquals(ThreeStateValue.zero, tt.getResult(0).get(0));
        assertEquals(ThreeStateValue.zero, tt.getResult(0).get(1));
        assertEquals(ThreeStateValue.zero, tt.getResult(0).get(2));
        assertEquals(ThreeStateValue.one, tt.getResult(0).get(3));
    }

    public void testSimpleDC() throws IOException {
        TruthTable tt = CSVImporter.readCSV("A,B,,Y\n0,0,,0\n0,1,,0\n1,0,,1\n1,1,,x");
        assertNotNull(tt);

        ArrayList<Variable> vars = tt.getVars();
        assertEquals(2, vars.size());
        assertEquals("A", vars.get(0).getIdentifier());
        assertEquals("B", vars.get(1).getIdentifier());
        assertEquals(1, tt.getResultCount());
        assertEquals("Y", tt.getResultName(0));

        assertEquals(ThreeStateValue.zero, tt.getResult(0).get(0));
        assertEquals(ThreeStateValue.zero, tt.getResult(0).get(1));
        assertEquals(ThreeStateValue.one, tt.getResult(0).get(2));
        assertEquals(ThreeStateValue.dontCare, tt.getResult(0).get(3));
    }

    public void testDC() throws IOException {
        TruthTable tt = CSVImporter.readCSV("A,B,,Y,X\nx,x,,1,0\n1,x,,0,1");
        assertNotNull(tt);

        ArrayList<Variable> vars = tt.getVars();
        assertEquals(2, vars.size());
        assertEquals("A", vars.get(0).getIdentifier());
        assertEquals("B", vars.get(1).getIdentifier());
        assertEquals(2, tt.getResultCount());
        assertEquals("Y", tt.getResultName(0));
        assertEquals("X", tt.getResultName(1));

        assertEquals(ThreeStateValue.one, tt.getResult(0).get(0));
        assertEquals(ThreeStateValue.one, tt.getResult(0).get(1));
        assertEquals(ThreeStateValue.one, tt.getResult(0).get(2));
        assertEquals(ThreeStateValue.one, tt.getResult(0).get(3));

        assertEquals(ThreeStateValue.zero, tt.getResult(1).get(0));
        assertEquals(ThreeStateValue.zero, tt.getResult(1).get(1));
        assertEquals(ThreeStateValue.one, tt.getResult(1).get(2));
        assertEquals(ThreeStateValue.one, tt.getResult(1).get(3));
    }

    public void testDC2() throws IOException {
        TruthTable tt = CSVImporter.readCSV("A,B,C,,Y\nx,1,x,,1\n");
        assertNotNull(tt);

        ArrayList<Variable> vars = tt.getVars();
        assertEquals(3, vars.size());
        assertEquals("A", vars.get(0).getIdentifier());
        assertEquals("B", vars.get(1).getIdentifier());
        assertEquals("C", vars.get(2).getIdentifier());
        assertEquals(1, tt.getResultCount());
        assertEquals("Y", tt.getResultName(0));

        assertEquals(ThreeStateValue.zero, tt.getResult(0).get(0));
        assertEquals(ThreeStateValue.zero, tt.getResult(0).get(1));
        assertEquals(ThreeStateValue.one, tt.getResult(0).get(2));
        assertEquals(ThreeStateValue.one, tt.getResult(0).get(3));
        assertEquals(ThreeStateValue.zero, tt.getResult(0).get(4));
        assertEquals(ThreeStateValue.zero, tt.getResult(0).get(5));
        assertEquals(ThreeStateValue.one, tt.getResult(0).get(6));
        assertEquals(ThreeStateValue.one, tt.getResult(0).get(7));
    }

    public void testMultiplePrimeUsages() throws IOException {
        TruthTable tt = CSVImporter.readCSV("A,B,,Y,X\n1,1,,1,1\n1,0,,0,1\n0,1,,0,1");
        assertNotNull(tt);

        ArrayList<Variable> vars = tt.getVars();
        assertEquals(2, vars.size());
        assertEquals("A", vars.get(0).getIdentifier());
        assertEquals("B", vars.get(1).getIdentifier());
        assertEquals(2, tt.getResultCount());
        assertEquals("Y", tt.getResultName(0));
        assertEquals("X", tt.getResultName(1));

        assertEquals(ThreeStateValue.zero, tt.getResult(0).get(0));
        assertEquals(ThreeStateValue.zero, tt.getResult(0).get(1));
        assertEquals(ThreeStateValue.zero, tt.getResult(0).get(2));
        assertEquals(ThreeStateValue.one, tt.getResult(0).get(3));

        assertEquals(ThreeStateValue.zero, tt.getResult(1).get(0));
        assertEquals(ThreeStateValue.one, tt.getResult(1).get(1));
        assertEquals(ThreeStateValue.one, tt.getResult(1).get(2));
        assertEquals(ThreeStateValue.one, tt.getResult(1).get(3));
    }

    public void testBug1() throws IOException {
        TruthTable tt = CSVImporter.readCSV("A,B,,Y,X,Z\n1,1,,1,1,1\n1,0,,0,1,1\n0,1,,0,1,1");
        assertNotNull(tt);

        try {
            CSVImporter.readCSV("A,B,,Y,X,Z\n1,1,,1,1,1\n1,0,,0,1,1,1\n0,1,,0,1,1");
            fail();
        } catch (IOException e) {
            assertTrue(true);
        }
    }

    public void testIncomplete() {
        try {
            CSVImporter.readCSV("\n\n");
            fail();
        } catch (IOException e) {
            assertTrue(true);
        }

        try {
            CSVImporter.readCSV("A,B,Y,X\n1,");
            fail();
        } catch (IOException e) {
            assertTrue(true);
        }

        try {
            CSVImporter.readCSV("A,B,Y,,\n1,");
            fail();
        } catch (IOException e) {
            assertTrue(true);
        }

        try {
            CSVImporter.readCSV("A,B,,Y,X\n1,");
            fail();
        } catch (IOException e) {
            assertTrue(true);
        }

        try {
            CSVImporter.readCSV("A,B,,Y,X\n1,1,1");
            fail();
        } catch (IOException e) {
            assertTrue(true);
        }

        try {
            CSVImporter.readCSV("A,B,,Y,X\n1,1,,1,1,1");
            fail();
        } catch (IOException e) {
            assertTrue(true);
        }
    }


    public void testAdder() throws Exception {
        loopTest("A_0,A_1,B_0,B_1,,S_1,S_0,C\n" +
                "1,0,1,0,,1,0,0\n" +
                "1,1,1,1,,1,0,0\n" +
                "0,0,X,1,,1,0,0\n" +
                "0,1,X,0,,1,0,0\n" +
                "X,0,0,1,,1,0,0\n" +
                "X,1,0,0,,1,0,0\n" +
                "0,X,1,X,,0,1,0\n" +
                "1,X,0,X,,0,1,0\n" +
                "1,X,1,1,,0,0,1\n" +
                "1,1,1,X,,0,0,1\n" +
                "X,1,X,1,,0,0,1\n");
    }

    public void testAndOr() throws Exception {
        loopTest("A,B,,X,Y\n" +
                "1,1,,1,0\n" +
                "1,X,,0,1\n" +
                "X,1,,0,1\n");
    }

    private void loopTest(String csv) throws Exception {
        TruthTable tt = CSVImporter.readCSV(csv);

        ExpressionListenerCSVCondensed elCSV = new ExpressionListenerCSVCondensed();
        for (int i = 0; i < tt.getResultCount(); i++) {
            MinimizerInterface mi = new MinimizerQuineMcCluskey();
            mi.minimize(tt.getVars(), tt.getResult(i), tt.getResultName(i), elCSV);
        }
        elCSV.close();

        assertEquals(csv, elCSV.toString());

    }
}