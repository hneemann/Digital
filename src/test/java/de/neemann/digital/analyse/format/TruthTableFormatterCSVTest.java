/*
 * Copyright (c) 2021 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.format;

import de.neemann.digital.analyse.TruthTable;
import de.neemann.digital.analyse.expression.ExpressionException;
import de.neemann.digital.analyse.quinemc.BoolTableByteArray;
import junit.framework.TestCase;

public class TruthTableFormatterCSVTest extends TestCase {

    public void testSimple() throws ExpressionException {
        TruthTable tt = new TruthTable(2);
        tt.addResult("X", new BoolTableByteArray(new byte[]{0, 1, 1, 1}));
        tt.addResult("Y", new BoolTableByteArray(new byte[]{0, 0, 0, 1}));
        String res = new TruthTableFormatterCSV().format(tt);

        assertEquals("A,B,,X,Y\n" +
                "0,0,,0,0\n" +
                "0,1,,1,0\n" +
                "1,0,,1,0\n" +
                "1,1,,1,1\n", res);
    }
}