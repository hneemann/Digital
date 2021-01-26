/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.quinemc;


import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.analyse.expression.format.FormatToExpression;
import de.neemann.digital.analyse.expression.format.FormatterException;
import junit.framework.TestCase;

import java.util.ArrayList;

/**
 */
public class QuineMcCluskeyRowTest extends TestCase {

    public void testSimple() throws FormatterException {
        ArrayList<Variable> vars = Variable.vars("A", "B", "C", "D");

        TableRow tr = new TableRow(4, 15, 0, false);
        assertEquals("A && B && C && D", FormatToExpression.JAVA.format(tr.getExpression(vars)));

        tr = new TableRow(4, 5, 0, false);
        assertEquals("!A && B && !C && D", FormatToExpression.JAVA.format(tr.getExpression(vars)));
        tr = new TableRow(4, 10, 0, false);
        assertEquals("A && !B && C && !D", FormatToExpression.JAVA.format(tr.getExpression(vars)));
        tr = new TableRow(4, 10, 0, false);
        tr.setToOptimized(2);
        assertEquals("A && !B && !D", FormatToExpression.JAVA.format(tr.getExpression(vars)));
        tr = new TableRow(4, 10, 0, false);
        tr.setToOptimized(0);
        assertEquals("!B && C && !D", FormatToExpression.JAVA.format(tr.getExpression(vars)));

    }
}
