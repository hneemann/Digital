/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.quinemc;


import de.neemann.digital.analyse.MinimizerInterface;
import de.neemann.digital.analyse.MinimizerQuineMcCluskey;
import de.neemann.digital.analyse.MinimizerQuineMcCluskeyExam;
import de.neemann.digital.analyse.expression.ContextFiller;
import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.ExpressionException;
import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.analyse.expression.format.FormatterException;
import de.neemann.digital.gui.components.table.ExpressionListenerStore;
import junit.framework.TestCase;

import java.util.ArrayList;

import static de.neemann.digital.analyse.expression.Variable.vars;

/**
 */
public class MinimizerRegressionTest extends TestCase {

    public void testQuineMcCluskey() throws ExpressionException, FormatterException {
        MinimizerInterface m = new MinimizerQuineMcCluskey();
        performTests(m);
    }

    public void testQuineMcCluskeyExam() throws ExpressionException, FormatterException {
        MinimizerInterface m = new MinimizerQuineMcCluskeyExam();
        performTests(m);
    }

    private void performTests(MinimizerInterface m) throws ExpressionException, FormatterException {
        performFull(m);
        performRegression(m);
    }

    /**
     * up to 3 variables we can calculate all tables possible!
     *
     * @throws ExpressionException
     */
    public void performFull(MinimizerInterface minimizer) throws ExpressionException, FormatterException {
        new FullVariantDontCareCreator() {
            @Override
            public void handleTable(int n, byte[] tab) throws ExpressionException, FormatterException {
                performTestCalculation(n, tab, minimizer);
            }
        }.create();
    }


    /**
     * for more than 3 variables we only test some random tables
     *
     * @throws ExpressionException
     */
    public void performRegression(MinimizerInterface minimizer) throws ExpressionException, FormatterException {
        int numOfTest = 2048;
        for (int n = 4; n <= 8; n++) {
            // test some tables with n variables
            // System.out.println(n + " vars: " + numOfTest + "tests");
            for (int i = 0; i < numOfTest; i++) {
                performTestCalculationRandom(n, minimizer);
            }
            numOfTest /= 4;
        }
    }

    static private void performTestCalculationRandom(int n, MinimizerInterface minimizer) throws ExpressionException, FormatterException {
        byte[] tab = new byte[1 << n];
        for (int i = 0; i < tab.length; i++)
            tab[i] = (byte) Math.round(Math.random() * 3); // half of the values are don't care

        performTestCalculation(n, tab, minimizer);
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
    static private void performTestCalculation(int n, byte[] tab, MinimizerInterface minimizer) throws ExpressionException, FormatterException {
        ArrayList<Variable> v = vars(n);

        final ExpressionListenerStore listener = new ExpressionListenerStore(null);
        minimizer.minimize(v, new BoolTableByteArray(tab), "Y", listener);
        Expression e = listener.getFirst();

        assertNotNull(e);

        ContextFiller context = new ContextFiller(v);
        for (int i = 0; i < tab.length; i++) {
            if (tab[i] <= 1)
                assertEquals(tab[i] == 1, e.calculate(context.setContextTo(i)));
        }
    }

}
