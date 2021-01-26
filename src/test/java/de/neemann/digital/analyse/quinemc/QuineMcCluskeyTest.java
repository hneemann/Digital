/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.quinemc;


import de.neemann.digital.analyse.expression.*;
import de.neemann.digital.analyse.expression.format.FormatToExpression;
import de.neemann.digital.analyse.expression.format.FormatterException;
import de.neemann.digital.analyse.quinemc.primeselector.PrimeSelectorDefault;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;

import static de.neemann.digital.analyse.expression.Not.not;
import static de.neemann.digital.analyse.expression.Operation.and;
import static de.neemann.digital.analyse.expression.Operation.or;
import static de.neemann.digital.analyse.expression.Variable.vars;
import static de.neemann.digital.analyse.expression.format.FormatToExpression.UNICODE;

/**
 */
public class QuineMcCluskeyTest extends TestCase {

    public void testDontCare() throws Exception, FormatterException {
        ArrayList<Variable> v = vars("A", "B", "C");
        Expression e = new QuineMcCluskey(v)
                .fillTableWith(new BoolTableByteArray(new byte[]{1, 1, 0, 0, 1, 2, 2, 0}))
                .simplify()
                .getExpression();

        assertEquals("!B", FormatToExpression.JAVA.format(e));
    }

    public void testGenerator() throws ExpressionException {
        Variable a = new Variable("A");// Vorlesung
        Variable b = new Variable("B");
        Variable c = new Variable("C");
        Variable d = new Variable("D");

        Expression e = or(and(a, and(c, d)), or(and(not(c), not(d)), and(not(b), c)));
        QuineMcCluskey t = new QuineMcCluskey(e);

        assertEquals(
                "0000,1\n" +
                        "1000,5\n" +
                        "0100,4\n" +
                        "1100,8\n" +
                        "0010,2\n" +
                        "1010,6\n" +
                        "0011,3\n" +
                        "1011,7\n" +
                        "1111,9\n", t.toString());

    }

    public void testSimplify() throws ExpressionException, FormatterException {
        Variable a = new Variable("A");// Vorlesung
        Variable b = new Variable("B");
        Variable c = new Variable("C");
        Variable d = new Variable("D");

        Expression e = or(and(a, c, d), and(not(c), not(d)), and(not(b), c));
        QuineMcCluskey t = new QuineMcCluskey(e);
        t.simplifyStep();
        assertFalse(t.isFinished());

        assertEquals(
                "-000,1,5\n" +
                        "-100,4,8\n" +
                        "-010,2,6\n" +
                        "-011,3,7\n" +
                        "0-00,1,4\n" +
                        "1-00,5,8\n" +
                        "1-11,7,9\n" +
                        "00-0,1,2\n" +
                        "10-0,5,6\n" +
                        "001-,2,3\n" +
                        "101-,6,7\n",
                t.toString());

        t.simplifyStep();
        assertFalse(t.isFinished());

        /*
        assertEquals(
                "--00,1,4,5,8\n" +
                        "--00,1,4,5,8\n" +
                        "-0-0,1,2,5,6\n" +
                "-0-0,1,2,5,6\n" +
                "-01-,2,3,6,7\n" +
                "-01-,2,3,6,7\n", t.toString());

        ArrayList<TableRow> primes = t.getPrimes();
        assertEquals(1, primes.size());
        assertEquals("1-11,7,9", primes.get(0).toString());

        t = t.removeDuplicates();*/
        assertFalse(t.isFinished());

        assertEquals(
                "--00,1,4,5,8\n" +
                        "-0-0,1,2,5,6\n" +
                        "-01-,2,3,6,7\n", t.toString());

        t.simplifyStep();
        assertTrue(t.isFinished());

        assertEquals("", t.toString());

        ArrayList<TableRow> primes = t.getPrimes();
        assertEquals(4, primes.size());
        assertEquals("1-11,7,9", primes.get(0).toString());
        assertEquals("-0-0,1,2,5,6", primes.get(2).toString());
        assertEquals("--00,1,4,5,8", primes.get(1).toString());
        assertEquals("-01-,2,3,6,7", primes.get(3).toString());

        Expression exp = t.getExpression();
        assertEquals("(A && C && D) || (!B && !D) || (!B && C) || (!C && !D)", FormatToExpression.JAVA.format(exp));

        t.simplifyPrimes(new PrimeSelectorDefault());

        exp = t.getExpression();
        assertEquals("(A && C && D) || (!B && C) || (!C && !D)", FormatToExpression.JAVA.format(exp));
    }

    public void testSimplify2() throws ExpressionException, FormatterException {
        QuineMcCluskey t = new QuineMcCluskey(Variable.vars("A", "B", "C", "D"));
        t.fillTableWith(new BoolTableByteArray(new byte[]{1, 1, 0, 0, 1, 1, 0, 1, 0, 0, 0, 0, 1, 1, 0, 1}));
        t = t.simplify();

        assertEquals("(!A && !C) || (B && D) || (B && !C)", FormatToExpression.JAVA.format(t.getExpression()));
    }

    public void testZero() throws Exception {
        QuineMcCluskey t = new QuineMcCluskey(Variable.vars("A", "B", "C"));
        t.fillTableWith(new BoolTableByteArray(new byte[]{0, 0, 0, 0, 0, 0, 0, 0}));
        t = t.simplify();
        Expression e = t.getExpression();
        assertNotNull(e);

        assertTrue(e instanceof Constant);
        assertFalse(((Constant) e).getValue());
    }

    public void testZero2() throws Exception {
        QuineMcCluskey t = new QuineMcCluskey(Variable.vars("A", "B", "C"));
        t.fillTableWith(new BoolTableByteArray(new byte[]{0, 0, 0, 0, 0, 0, 2, 2}));
        t = t.simplify();
        Expression e = t.getExpression();
        assertNotNull(e);

        assertTrue(e instanceof Constant);
        assertFalse(((Constant) e).getValue());
    }

    public void testOne() throws Exception {
        QuineMcCluskey t = new QuineMcCluskey(Variable.vars("A", "B", "C"));
        t.fillTableWith(new BoolTableByteArray(new byte[]{1, 1, 1, 1, 1, 1, 1, 1}));
        t = t.simplify();
        Expression e = t.getExpression();
        assertNotNull(e);

        assertTrue(e instanceof Constant);
        assertTrue(((Constant) e).getValue());
    }

    public void testOne2() throws Exception {
        QuineMcCluskey t = new QuineMcCluskey(Variable.vars("A", "B", "C"));
        t.fillTableWith(new BoolTableByteArray(new byte[]{1, 1, 1, 1, 1, 1, 2, 2}));
        t = t.simplify();
        Expression e = t.getExpression();
        assertNotNull(e);

        assertTrue(e instanceof Constant);
        assertTrue(((Constant) e).getValue());
    }

    public void testMultipleResults() throws ExpressionException {
        QuineMcCluskeyExam t = new QuineMcCluskeyExam(Variable.vars("A", "B", "C"));
        t.fillTableWith(new BoolTableByteArray(new byte[]{0, 1, 1, 0, 0, 1, 1, 1}));
        PrimeSelectorDefault ps = new PrimeSelectorDefault();
        t.simplify(ps);

        assertEquals(2, ps.getAllSolutions().size());
    }

    public void testComplexity() throws Exception {
        new FullVariantDontCareCreator() {
            @Override
            public void handleTable(int n, byte[] tab) throws ExpressionException, FormatterException {
                Expression e = createExpression(n, tab);

                byte[] tabZero = Arrays.copyOf(tab, tab.length);
                for (int i = 0; i < tabZero.length; i++)
                    if (tabZero[i] > 1) tabZero[i] = 0;
                Expression eZero = createExpression(n, tabZero);

                byte[] tabOne = Arrays.copyOf(tab, tab.length);
                for (int i = 0; i < tabOne.length; i++)
                    if (tabOne[i] > 1) tabOne[i] = 1;

                Expression eOne = createExpression(n, tabOne);

                int c = e.traverse(new ComplexityVisitor()).getComplexity();
                int cOne = eOne.traverse(new ComplexityVisitor()).getComplexity();
                int cZero = eZero.traverse(new ComplexityVisitor()).getComplexity();

                boolean ok = (c <= cOne) && (c <= cZero);
                if (!ok) {
                    System.out.println("\nX: " + UNICODE.format(e) + ", " + c);
                    System.out.println("0: " + UNICODE.format(eZero) + ", " + cZero);
                    System.out.println("1: " + UNICODE.format(eOne) + ", " + cOne);

                    assertTrue(false);
                }
            }
        }.create();
    }

    private Expression createExpression(int n, byte[] tab) throws ExpressionException {
        ArrayList<Variable> v = vars(n);
        return new QuineMcCluskey(v)
                .fillTableWith(new BoolTableByteArray(tab))
                .simplify()
                .getExpression();
    }

}
