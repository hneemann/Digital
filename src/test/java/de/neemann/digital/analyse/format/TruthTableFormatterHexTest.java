/*
 * Copyright (c) 2021 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.format;

import de.neemann.digital.analyse.TruthTable;
import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.analyse.quinemc.BoolTableByteArray;
import junit.framework.TestCase;

import java.io.StringWriter;
import java.util.ArrayList;

public class TruthTableFormatterHexTest extends TestCase {

    public void testHexExportSingle() throws Exception {
        ArrayList<Variable> vars = Variable.vars(3);
        TruthTable t = new TruthTable(vars).addResult();
        BoolTableByteArray result = (BoolTableByteArray) t.getResult(0);
        for (int i = 0; i < t.getRows(); i++) {
            result.set(i, i % 2);
        }
        String hex = new TruthTableFormatterHex().format(t);

        assertEquals("v2.0 raw\n" +
                "0\n" +
                "1\n" +
                "0\n" +
                "1\n" +
                "0\n" +
                "1\n" +
                "0\n" +
                "1\n", hex);
    }

    public void testHexExportTwo() throws Exception {
        ArrayList<Variable> vars = Variable.vars(3);
        TruthTable t = new TruthTable(vars).addResult();
        BoolTableByteArray result = (BoolTableByteArray) t.getResult(0);
        for (int i = 0; i < t.getRows(); i++) {
            result.set(i, i % 2);
        }
        t.addResult();
        result = (BoolTableByteArray) t.getResult(1);
        for (int i = 0; i < t.getRows(); i++) {
            result.set(i, (i + 1) % 2);
        }

        String hex = new TruthTableFormatterHex().format(t);

        assertEquals("v2.0 raw\n" +
                "2\n" +
                "1\n" +
                "2\n" +
                "1\n" +
                "2\n" +
                "1\n" +
                "2\n" +
                "1\n", hex);
    }

}