/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.quinemc;

import de.neemann.digital.analyse.MinimizerQuineMcCluskeyExam;
import de.neemann.digital.analyse.expression.ComplexityVisitor;
import de.neemann.digital.analyse.expression.ExpressionException;
import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.analyse.expression.format.FormatterException;
import de.neemann.digital.gui.components.table.ExpressionListenerStore;
import junit.framework.TestCase;

public class QuineMcCluskeyExamTest extends TestCase {

    public void testMinimal() throws ExpressionException, FormatterException {
        ExpressionListenerStore results = new ExpressionListenerStore(null);
        new MinimizerQuineMcCluskeyExam().minimize(
                Variable.vars(4),
                new BoolTableByteArray(new byte[]{1, 2, 1, 1, 1, 0, 0, 0, 1, 1, 0, 1, 0, 0, 1, 1}),
                "Y",
                results);

        assertEquals(1, results.getResults().size());
    }

    public void testMultipleSolutions() throws ExpressionException, FormatterException {
        ExpressionListenerStore results = new ExpressionListenerStore(null);
        new MinimizerQuineMcCluskeyExam().minimize(
                Variable.vars(4),
                new BoolTableByteArray(new byte[]{2, 0, 0, 0, 1, 2, 0, 0, 1, 1, 2, 0, 1, 1, 1, 2}),
                "Y",
                results);

        assertEquals(4, results.getResults().size());
        int compl = -1;
        for (ExpressionListenerStore.Result r : results.getResults()) {
            assertEquals("Y", r.getName());
            int c = r.getExpression().traverse(new ComplexityVisitor()).getComplexity();
            if (compl < 0) compl = c;
            assertEquals(compl, c);
        }
    }

}
