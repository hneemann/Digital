package de.neemann.digital.analyse.quinemc;


import de.neemann.digital.analyse.expression.*;
import de.neemann.digital.analyse.expression.format.FormatToExpression;
import de.neemann.digital.analyse.expression.format.FormatterException;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;

import static de.neemann.digital.analyse.expression.Variable.vars;
import static de.neemann.digital.analyse.expression.format.FormatToExpression.FORMATTER_UNICODE;

/**
 * @author hneemann
 */
public class QuineMcCluskeyDontCareTest extends TestCase {

    public void testDontCare() throws Exception, FormatterException {
        ArrayList<Variable> v = vars("A", "B", "C");
        Expression e = new QuineMcCluskey(v)
                .fillTableWith(new BoolTableIntArray(new int[]{1, 1, 0, 0, 1, 2, 2, 0}))
                .simplify()
                .getExpression();

        assertEquals("!B", FormatToExpression.FORMATTER_JAVA.format(e));
    }

    /**
     * up to 3 variables we can calculate all tables possible!
     *
     * @throws ExpressionException
     */
    public void testFull() throws ExpressionException, FormatterException {
        new FullVariantDontCareCreator() {
            @Override
            public void handleTable(int n, int[] tab) throws ExpressionException {
                performTestCalculation(n, tab);
            }
        }.create();
    }


    /**
     * for more then 3 variables we only test some random tables
     *
     * @throws ExpressionException
     */
    public void testRegression() throws ExpressionException {
        int numOfTest = 2048;
        for (int n = 4; n <= 8; n++) {
            // test some tables with n variables
            // System.out.println(n + " vars: " + numOfTest + "tests");
            for (int i = 0; i < numOfTest; i++) {
                performTestCalculationRandom(n);
            }
            numOfTest /= 4;
        }
    }

    static private void performTestCalculationRandom(int n) throws ExpressionException {
        int[] tab = new int[1 << n];
        for (int i = 0; i < tab.length; i++)
            tab[i] = (int) Math.round(Math.random() * 3); // half of the values are don't care

        performTestCalculation(n, tab);
    }

    /**
     * Generates a expression from the table and then checks if
     * the expression reproduces the given table.
     * Does not test if the expression is minimal.
     *
     * @param n   the number of variables
     * @param tab the truth table
     * @throws ExpressionException
     */
    static private void performTestCalculation(int n, int[] tab) throws ExpressionException {
        ArrayList<Variable> v = vars(n);
        Expression e = new QuineMcCluskey(v)
                .fillTableWith(new BoolTableIntArray(tab))
                .simplify()
                .getExpression();

        assertNotNull(e);

        ContextFiller context = new ContextFiller(v);
        for (int i = 0; i < tab.length; i++) {
            if (tab[i] <= 1)
                assertEquals(tab[i] == 1, e.calculate(context.setContextTo(i)));
        }
    }

    public void testComplexity() throws Exception {
        new FullVariantDontCareCreator() {
            @Override
            public void handleTable(int n, int[] tab) throws ExpressionException, FormatterException {
                Expression e = createExpression(n, tab);

                int[] tabZero = Arrays.copyOf(tab, tab.length);
                for (int i = 0; i < tabZero.length; i++)
                    if (tabZero[i] > 1) tabZero[i] = 0;
                Expression eZero = createExpression(n, tabZero);

                int[] tabOne = Arrays.copyOf(tab, tab.length);
                for (int i = 0; i < tabOne.length; i++)
                    if (tabOne[i] > 1) tabOne[i] = 1;

                Expression eOne = createExpression(n, tabOne);

                int c = e.traverse(new ComplexityVisitor()).getComplexity();
                int cOne = eOne.traverse(new ComplexityVisitor()).getComplexity();
                int cZero = eZero.traverse(new ComplexityVisitor()).getComplexity();

                boolean ok = (c <= cOne) && (c <= cZero);
                if (!ok) {
                    System.out.println("\nX: " + FORMATTER_UNICODE.format(e) + ", " + c);
                    System.out.println("0: " + FORMATTER_UNICODE.format(eZero) + ", " + cZero);
                    System.out.println("1: " + FORMATTER_UNICODE.format(eOne) + ", " + cOne);

                    assertTrue(false);
                }
            }
        }.create();
    }

    private Expression createExpression(int n, int[] tab) throws ExpressionException {
        ArrayList<Variable> v = vars(n);
        return new QuineMcCluskey(v)
                .fillTableWith(new BoolTableIntArray(tab))
                .simplify()
                .getExpression();
    }
}