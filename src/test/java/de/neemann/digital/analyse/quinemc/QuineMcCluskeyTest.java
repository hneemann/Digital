package de.neemann.digital.analyse.quinemc;


import de.neemann.digital.analyse.expression.Constant;
import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.ExpressionException;
import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.analyse.expression.format.FormatToExpression;
import de.neemann.digital.analyse.expression.format.FormatterException;
import de.neemann.digital.analyse.quinemc.primeselector.PrimeSelectorDefault;
import junit.framework.TestCase;

import java.util.ArrayList;

import static de.neemann.digital.analyse.expression.Not.not;
import static de.neemann.digital.analyse.expression.Operation.and;
import static de.neemann.digital.analyse.expression.Operation.or;

/**
 * @author hneemann
 */
public class QuineMcCluskeyTest extends TestCase {


    public void testGenerator() throws ExpressionException {
        Variable a = new Variable("A");// Vorlesung
        Variable b = new Variable("B");
        Variable c = new Variable("C");
        Variable d = new Variable("D");

        Expression e = or(and(a, and(c, d)), or(and(not(c), not(d)), and(not(b), c)));
        QuineMcCluskey t = new QuineMcCluskey(e);

        assertEquals(
                "0000,1\n" +
                "0010,2\n" +
                        "0011,3\n" +
                "0100,4\n" +
                "1000,5\n" +
                "1010,6\n" +
                        "1011,7\n" +
                "1100,8\n" +
                "1111,9\n", t.toString());

    }

    public void testSimplify() throws ExpressionException, FormatterException {
        Variable a = new Variable("A");// Vorlesung
        Variable b = new Variable("B");
        Variable c = new Variable("C");
        Variable d = new Variable("D");

        Expression e = or(and(a, and(c, d)), or(and(not(c), not(d)), and(not(b), c)));
        QuineMcCluskey t = new QuineMcCluskey(e).simplifyStep();
        assertFalse(t.isFinished());

        assertEquals(
                "-000,1,5\n" +
                        "-010,2,6\n" +
                        "-011,3,7\n" +
                        "-100,4,8\n" +
                        "0-00,1,4\n" +
                        "00-0,1,2\n" +
                "001-,2,3\n" +
                        "1-00,5,8\n" +
                        "1-11,7,9\n" +
                "10-0,5,6\n" +
                        "101-,6,7\n",
                t.toString());

        t = t.simplifyStep();
        assertFalse(t.isFinished());

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

        t = t.removeDuplicates();
        assertFalse(t.isFinished());

        assertEquals(
                "--00,1,4,5,8\n" +
                        "-0-0,1,2,5,6\n" +
                "-01-,2,3,6,7\n", t.toString());

        t = t.simplifyStep();
        assertTrue(t.isFinished());

        assertEquals("", t.toString());

        primes = t.getPrimes();
        assertEquals(4, primes.size());
        assertEquals("1-11,7,9", primes.get(0).toString());
        assertEquals("-0-0,1,2,5,6", primes.get(2).toString());
        assertEquals("--00,1,4,5,8", primes.get(1).toString());
        assertEquals("-01-,2,3,6,7", primes.get(3).toString());

        Expression exp = t.getExpression();
        assertEquals("(A && C && D) || (!B && !D) || (!B && C) || (!C && !D)", FormatToExpression.FORMATTER_JAVA.format(exp));

        t.simplifyPrimes(new PrimeSelectorDefault());

        exp = t.getExpression();
        assertEquals("(A && C && D) || (!B && C) || (!C && !D)", FormatToExpression.FORMATTER_JAVA.format(exp));
    }

    public void testSimplify2() throws ExpressionException, FormatterException {
        QuineMcCluskey t = new QuineMcCluskey(Variable.vars("A", "B", "C", "D"));
        t.fillTableWith(new BoolTableIntArray(new int[]{1, 1, 0, 0, 1, 1, 0, 1, 0, 0, 0, 0, 1, 1, 0, 1}));
        t = t.simplify();

        assertEquals("(!A && !C) || (B && D) || (B && !C)", FormatToExpression.FORMATTER_JAVA.format(t.getExpression()));
    }

    public void testZero() throws Exception {
        QuineMcCluskey t = new QuineMcCluskey(Variable.vars("A", "B", "C"));
        t.fillTableWith(new BoolTableIntArray(new int[]{0, 0, 0, 0, 0, 0, 0, 0}));
        t = t.simplify();
        Expression e = t.getExpression();
        assertNotNull(e);

        assertTrue(e instanceof Constant);
        assertFalse(((Constant) e).getValue());
    }

    public void testZero2() throws Exception {
        QuineMcCluskey t = new QuineMcCluskey(Variable.vars("A", "B", "C"));
        t.fillTableWith(new BoolTableIntArray(new int[]{0, 0, 0, 0, 0, 0, 2, 2}));
        t = t.simplify();
        Expression e = t.getExpression();
        assertNotNull(e);

        assertTrue(e instanceof Constant);
        assertFalse(((Constant) e).getValue());
    }

    public void testOne() throws Exception {
        QuineMcCluskey t = new QuineMcCluskey(Variable.vars("A", "B", "C"));
        t.fillTableWith(new BoolTableIntArray(new int[]{1, 1, 1, 1, 1, 1, 1, 1}));
        t = t.simplify();
        Expression e = t.getExpression();
        assertNotNull(e);

        assertTrue(e instanceof Constant);
        assertTrue(((Constant) e).getValue());
    }

    public void testOne2() throws Exception {
        QuineMcCluskey t = new QuineMcCluskey(Variable.vars("A", "B", "C"));
        t.fillTableWith(new BoolTableIntArray(new int[]{1, 1, 1, 1, 1, 1, 2, 2}));
        t = t.simplify();
        Expression e = t.getExpression();
        assertNotNull(e);

        assertTrue(e instanceof Constant);
        assertTrue(((Constant) e).getValue());
    }
}