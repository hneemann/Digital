/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.quinemc;

import de.neemann.digital.analyse.expression.ContextFiller;
import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.analyse.expression.format.FormatToExpression;
import de.neemann.digital.analyse.quinemc.primeselector.PrimeSelectorDefault;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collections;

import static de.neemann.digital.analyse.expression.Operation.or;

/**
 */
public class QuineMcCluskeyRegressionTest extends TestCase {

    public void testRegression() throws Exception {
        testRegression(8, 128);
        testRegression(8, 16);
        testRegression(4, 8);
        testRegression(4, 4);
    }

    public void testRegression2() throws Exception {
        for (int i = 0; i < 100; i++) {
            testRegression(5, 16);
            testRegression(5, 8);
            testRegression(5, 4);
        }
    }

    public void testRegression3() throws Exception {
        Variable a = new Variable("A");
        Variable b = new Variable("B");
        Variable c = new Variable("C");
        Variable d = new Variable("D");
        ArrayList<Variable> vars = new ArrayList<>();
        vars.add(a);
        vars.add(b);
        vars.add(c);
        vars.add(d);
        QuineMcCluskey t = new QuineMcCluskey(vars);

        Expression ex = or(a, c);
        t.fillTableWith(new BoolTableExpression(ex, new ContextFiller(vars)));

//        System.out.println("--");
        while (!t.isFinished()) {
//            System.out.println(FormatToExpression.FORMATTER_JAVA.format(t.getExpression()));
            t.simplifyStep();
        }
        t.simplifyPrimes(new PrimeSelectorDefault());
        assertEquals("A || C", FormatToExpression.JAVA.format(t.getExpression()));
//        System.out.println("--");
    }


    private static void testRegression(int n, int j) throws Exception {
        int size = 1 << n;
        boolean[] table = new boolean[size];

        ArrayList<Integer> index = new ArrayList<>();
        for (int i = 0; i < size; i++) index.add(i);
        Collections.shuffle(index);

        for (int i = 0; i < j; i++)
            table[index.get(i)] = true;

        ArrayList<Variable> var = Variable.vars(n);

        Expression expression =
                new QuineMcCluskey(var)
                        .fillTableWith(new BoolTableBoolArray(table))
                        .simplify()
                        .getExpression();

        ContextFiller cf = new ContextFiller(var);

        for (int i = 0; i < table.length; i++)
            assertEquals(table[i], expression.calculate(cf.setContextTo(i)));
    }
}
