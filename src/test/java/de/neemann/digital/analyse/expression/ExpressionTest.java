/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.expression;

import junit.framework.TestCase;

import static de.neemann.digital.analyse.expression.Not.not;
import static de.neemann.digital.analyse.expression.Operation.and;
import static de.neemann.digital.analyse.expression.Operation.or;


/**
 */
public class ExpressionTest extends TestCase {

    public void testCalculate() throws Exception {
        Variable a = new Variable("A");
        Variable b = new Variable("B");
        Expression e = and(not(or(not(a), not(b))), not(and(not(a), not(b))));

        ContextFiller fc = new ContextFiller(e);
        assertEquals(false, e.calculate(fc.setContextTo(0)));
        assertEquals(false, e.calculate(fc.setContextTo(1)));
        assertEquals(false, e.calculate(fc.setContextTo(2)));
        assertEquals(true, e.calculate(fc.setContextTo(3)));
    }

    /**
     * public void test2() throws FormatterException, ExpressionException {
     * Variable a = new Variable("A");// Vorlesung
     * Variable b = new Variable("B");
     * Variable c = new Variable("C");
     * Variable d = new Variable("D");
     * <p>
     * Expression e = or(and(a, and(c, d)), or(and(not(c), not(d)), and(not(b), c)));
     * String out = FormatToExpression.FORMATTER_LATEX.format(e);
     * assertEquals("(A \\und C \\und D) \\oder (\\nicht{B} \\und C) \\oder (\\nicht{C} \\und \\nicht{D})", out);
     * <p>
     * ContextFiller fc = new ContextFiller(e);
     * <p>
     * int[] vector = new int[]{1, 0, 1, 1, 1, 0, 0, 0, 1, 0, 1, 1, 1, 0, 0, 1};
     * for (int i = 0; i < vector.length; i++)
     * assertEquals(vector[i] == 1, e.calculate(fc.setContextTo(i)));
     * }
     */

    public void testConstant() throws ExpressionException {
        Variable a = new Variable("A");
        Expression e = or(a, Constant.ONE);

        ContextMap c = new ContextMap();
        assertTrue(e.calculate(c.set(a, true)));
        assertTrue(e.calculate(c.set(a, false)));

        e = and(a, Constant.ONE);
        assertTrue(e.calculate(c.set(a, true)));
        assertFalse(e.calculate(c.set(a, false)));

        e = or(a, Constant.ZERO);
        assertTrue(e.calculate(c.set(a, true)));
        assertFalse(e.calculate(c.set(a, false)));

    }
}
