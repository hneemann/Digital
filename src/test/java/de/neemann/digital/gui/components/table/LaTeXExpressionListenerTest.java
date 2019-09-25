/*
 * Copyright (c) 2019 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.table;

import de.neemann.digital.analyse.TruthTable;
import de.neemann.digital.analyse.TruthTableTableModel;
import de.neemann.digital.analyse.expression.ExpressionException;
import de.neemann.digital.analyse.expression.Not;
import de.neemann.digital.analyse.expression.Operation;
import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.analyse.quinemc.BoolTableBoolArray;
import junit.framework.TestCase;

public class LaTeXExpressionListenerTest extends TestCase {

    public void testSimple() throws ExpressionException {
        TruthTable tt = new TruthTable()
                .addVariable("A")
                .addVariable("B")
                .addResult("Y", new BoolTableBoolArray(new boolean[]{false, false, true, false}));
        LaTeXExpressionListener l = new LaTeXExpressionListener(tt);
        l.resultFound("Y", Operation.and(new Variable("A"), new Not(new Variable("B"))));
        l.close();

        assertEquals("\\begin{center}\n" +
                "\\begin{tabular}{cc|c}\n" +
                "$A$&$B$&$Y$\\\\\n" +
                "\\hline\n" +
                "$0$&$0$&$0$\\\\\n" +
                "$0$&$1$&$0$\\\\\n" +
                "$1$&$0$&$1$\\\\\n" +
                "$1$&$1$&$0$\\\\\n" +
                "\\end{tabular}\n" +
                "\\end{center}\n" +
                "\\begin{eqnarray*}\n" +
                "Y &=& A \\und \\overline{B}\\\\\n" +
                "\\end{eqnarray*}\n", l.toString());
    }

}