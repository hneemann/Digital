/*
 * Copyright (c) 2021 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.table;

import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.ExpressionException;
import de.neemann.digital.analyse.expression.NamedExpression;
import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.analyse.expression.format.FormatterException;
import junit.framework.TestCase;

import static de.neemann.digital.analyse.expression.Not.not;
import static de.neemann.digital.analyse.expression.Operation.and;
import static de.neemann.digital.analyse.expression.Operation.or;

public class ExpressionListenerCSVCondensedTest extends TestCase {
    Variable a = new Variable("A");
    Variable b = new Variable("B");
    Variable c = new Variable("C");

    public void testSimple() throws FormatterException, ExpressionException {
        Expression expression = or(and(a, b), c);

        ExpressionListenerCSVCondensed el = create(expression);

        assertEquals("A,B,C,,Y\n" +
                "1,1,X,,1\n" +
                "X,X,1,,1\n", el.toString());
    }

    public void testNot() throws FormatterException, ExpressionException {
        Expression expression = or(and(a, not(b)), not(c));

        ExpressionListenerCSVCondensed el = create(expression);

        assertEquals("A,B,C,,Y\n" +
                "1,0,X,,1\n" +
                "X,X,0,,1\n", el.toString());
    }

    public void testTwo() throws FormatterException, ExpressionException {
        Expression e1 = new NamedExpression("Y", or(and(a, b), c));
        Expression e2 = new NamedExpression("X", a);

        ExpressionListenerCSVCondensed el = create(e1, e2);

        assertEquals("A,B,C,,Y,X\n" +
                "1,1,X,,1,0\n" +
                "X,X,1,,1,0\n" +
                "1,X,X,,0,1\n", el.toString());
    }

    private ExpressionListenerCSVCondensed create(Expression... expressions) throws FormatterException, ExpressionException {
        ExpressionListenerCSVCondensed el = new ExpressionListenerCSVCondensed();

        for (Expression e : expressions) {
            String name = "Y";
            if (e instanceof NamedExpression) {
                NamedExpression ne = (NamedExpression) e;
                name = ne.getName();
                e = ne.getExpression();
            }
            el.resultFound(name, e);
        }
        el.close();
        return el;
    }
}