/*
 * Copyright (c) 2019 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.format;

import de.neemann.digital.analyse.ModelAnalyser;
import de.neemann.digital.analyse.TruthTable;
import de.neemann.digital.core.Model;
import de.neemann.digital.integration.ToBreakRunner;
import junit.framework.TestCase;

public class TruthTableFormatterTestCaseTest extends TestCase {

    public void testFormat() throws Exception {
        TruthTable tt = new TruthTable(3);
        tt.addResult("Y_0");
        tt.addResult("Y_1");

        assertEquals("A B C Y_0 Y_1 \n" +
                "\n" +
                " 0 0 0 0 0\n" +
                " 0 0 1 0 0\n" +
                " 0 1 0 0 0\n" +
                " 0 1 1 0 0\n" +
                " 1 0 0 0 0\n" +
                " 1 0 1 0 0\n" +
                " 1 1 0 0 0\n" +
                " 1 1 1 0 0\n", new TruthTableFormatterTestCase(null).format(tt));
    }

    public void testBus() throws Exception {
        Model m = new ToBreakRunner("dig/analyze/testCaseCreation.dig").getModel();
        TruthTable tt = new ModelAnalyser(m).analyse();

        assertEquals("b a C_i C_o S \n" +
                "\n" +
                " 0b00 0b00 0 0 0b00\n" +
                " 0b00 0b00 1 0 0b01\n" +
                " 0b00 0b01 0 0 0b01\n" +
                " 0b00 0b01 1 0 0b10\n" +
                " 0b00 0b10 0 0 0b10\n" +
                " 0b00 0b10 1 0 0b11\n" +
                " 0b00 0b11 0 0 0b11\n" +
                " 0b00 0b11 1 1 0b00\n" +
                " 0b01 0b00 0 0 0b01\n" +
                " 0b01 0b00 1 0 0b10\n" +
                " 0b01 0b01 0 0 0b10\n" +
                " 0b01 0b01 1 0 0b11\n" +
                " 0b01 0b10 0 0 0b11\n" +
                " 0b01 0b10 1 1 0b00\n" +
                " 0b01 0b11 0 1 0b00\n" +
                " 0b01 0b11 1 1 0b01\n" +
                " 0b10 0b00 0 0 0b10\n" +
                " 0b10 0b00 1 0 0b11\n" +
                " 0b10 0b01 0 0 0b11\n" +
                " 0b10 0b01 1 1 0b00\n" +
                " 0b10 0b10 0 1 0b00\n" +
                " 0b10 0b10 1 1 0b01\n" +
                " 0b10 0b11 0 1 0b01\n" +
                " 0b10 0b11 1 1 0b10\n" +
                " 0b11 0b00 0 0 0b11\n" +
                " 0b11 0b00 1 1 0b00\n" +
                " 0b11 0b01 0 1 0b00\n" +
                " 0b11 0b01 1 1 0b01\n" +
                " 0b11 0b10 0 1 0b01\n" +
                " 0b11 0b10 1 1 0b10\n" +
                " 0b11 0b11 0 1 0b10\n" +
                " 0b11 0b11 1 1 0b11\n", new TruthTableFormatterTestCase(tt.getModelAnalyzerInfo()).format(tt));
    }

}