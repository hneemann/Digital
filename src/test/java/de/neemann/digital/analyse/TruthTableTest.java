/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse;

import de.neemann.digital.analyse.expression.ContextFiller;
import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.analyse.quinemc.BoolTableByteArray;
import junit.framework.TestCase;

import java.io.StringWriter;
import java.util.ArrayList;

/**
 */
public class TruthTableTest extends TestCase {

    public void testGetRows() throws Exception {
        TruthTable t = new TruthTable(3);

        assertEquals(8, t.getRows());
    }

    public void testGetByContext() throws Exception {
        ArrayList<Variable> vars = Variable.vars(5);
        TruthTable t = new TruthTable(vars).addResult();
        BoolTableByteArray result = (BoolTableByteArray) t.getResult(0);
        for (int i = 0; i < t.getRows(); i++) {
            result.set(i, i % 3);
        }

        ContextFiller fc = new ContextFiller(vars);
        for (int i = 0; i < t.getRows(); i++) {
            fc.setContextTo(i);
            assertEquals(i % 3, t.getByContext(0, fc));
        }
    }

    public void testHexExportSingle() throws Exception {
        ArrayList<Variable> vars = Variable.vars(3);
        TruthTable t = new TruthTable(vars).addResult();
        BoolTableByteArray result = (BoolTableByteArray) t.getResult(0);
        for (int i = 0; i < t.getRows(); i++) {
            result.set(i, i % 2);
        }
        StringWriter w = new StringWriter();
        t.saveHex(w);
        w.close();

        assertEquals("v2.0 raw\n" +
                "0\n" +
                "1\n" +
                "0\n" +
                "1\n" +
                "0\n" +
                "1\n" +
                "0\n" +
                "1\n", w.toString());
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
        StringWriter w = new StringWriter();
        t.saveHex(w);
        w.close();

        assertEquals("v2.0 raw\n" +
                "2\n" +
                "1\n" +
                "2\n" +
                "1\n" +
                "2\n" +
                "1\n" +
                "2\n" +
                "1\n", w.toString());
    }
}
