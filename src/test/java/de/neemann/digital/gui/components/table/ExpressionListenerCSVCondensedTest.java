/*
 * Copyright (c) 2021 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.table;

import de.neemann.digital.analyse.CSVImporter;
import de.neemann.digital.analyse.TruthTable;
import de.neemann.digital.analyse.expression.*;
import de.neemann.digital.analyse.expression.format.FormatterException;
import de.neemann.digital.analyse.quinemc.ThreeStateValue;
import junit.framework.TestCase;

import java.io.IOException;
import java.util.ArrayList;

import static de.neemann.digital.analyse.expression.Not.not;
import static de.neemann.digital.analyse.expression.Operation.and;
import static de.neemann.digital.analyse.expression.Operation.or;

public class ExpressionListenerCSVCondensedTest extends TestCase {
    Variable a = new Variable("A");
    Variable b = new Variable("B");
    Variable c = new Variable("C");

    public void testSimple() throws FormatterException, ExpressionException, IOException {
        Expression expression = or(and(a, b), c);

        assertEquals("A,B,C,,Y\n" +
                "1,1,X,,1\n" +
                "X,X,1,,1\n", create(expression));
    }

    public void testNot() throws FormatterException, ExpressionException, IOException {
        Expression expression = or(and(a, not(b)), not(c));

        assertEquals("A,B,C,,Y\n" +
                "1,0,X,,1\n" +
                "X,X,0,,1\n", create(expression));
    }

    public void testTwo() throws FormatterException, ExpressionException, IOException {
        Expression e1 = new NamedExpression("Y", or(and(a, b), c));
        Expression e2 = new NamedExpression("X", a);

        assertEquals("A,B,C,,Y,X\n" +
                "1,1,X,,1,0\n" +
                "X,X,1,,1,0\n" +
                "1,X,X,,0,1\n", create(e1, e2));
    }

    public void testXor() throws FormatterException, ExpressionException, IOException {
        Expression expression = or(
                and(not(a), not(b), c),
                and(not(a), b, not(c)),
                and(a, not(b), not(c)),
                and(a, b, c)
        );

        assertEquals("A,B,C,,Y\n" +
                "0,0,1,,1\n" +
                "0,1,0,,1\n" +
                "1,0,0,,1\n" +
                "1,1,1,,1\n", create(expression));
    }

    private String create(Expression... expressions) throws FormatterException, ExpressionException, IOException {
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
        String s = el.toString();
        loopCheck(s, expressions);
        return s;
    }

    private void loopCheck(String csv, Expression[] expressions) throws IOException, ExpressionException {
        TruthTable tt = CSVImporter.readCSV(csv);
        for (int e = 0; e < expressions.length; e++) {
            ArrayList<Variable> vars = tt.getVars();
            int count = 1 << vars.size();
            ContextFiller cf = new ContextFiller(vars);
            for (int i = 0; i < count; i++) {
                cf.setContextTo(i);
                boolean expected = expressions[e].calculate(cf);
                ThreeStateValue found = tt.getResult(e).get(i);
                assertEquals(ThreeStateValue.value(expected), found);
            }
        }
    }
}