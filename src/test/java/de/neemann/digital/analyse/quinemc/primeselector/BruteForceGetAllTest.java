/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.quinemc.primeselector;


import de.neemann.digital.analyse.expression.ContextFiller;
import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.ExpressionException;
import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.analyse.expression.format.FormatterException;
import de.neemann.digital.analyse.quinemc.BoolTableByteArray;
import de.neemann.digital.analyse.quinemc.FullVariantDontCareCreator;
import de.neemann.digital.analyse.quinemc.QuineMcCluskey;
import de.neemann.digital.analyse.quinemc.TableRow;
import junit.framework.TestCase;

import java.util.ArrayList;

import static de.neemann.digital.analyse.expression.Variable.vars;

/**
 */
public class BruteForceGetAllTest extends TestCase {

    /**
     * up to 3 variables we can calculate all tables possible!
     *
     * @throws ExpressionException
     */
    public void testFullRegression() throws ExpressionException, FormatterException {
        new FullVariantDontCareCreator() {
            @Override
            public void handleTable(int n, byte[] tab) throws ExpressionException {
                performTestCalculation(n, tab);
            }
        }.create();
        new FullVariantDontCareCreator(4, 241) {
            @Override
            public void handleTable(int n, byte[] tab) throws ExpressionException {
                performTestCalculation(n, tab);
            }
        }.create();
    }

    /*
    public void testFull() throws ExpressionException, FormatterException {
        new FullVariantDontCareCreator(4) {
            @Override
            public void handleTable(int n, int[] tab) throws ExpressionException {
                performTestCalculation(n, tab);
            }
        }.create();
    } /**/

    static private void performTestCalculation(int n, byte[] tab) throws ExpressionException {

        BruteForceGetAll ps = new BruteForceGetAll();

        ArrayList<Variable> v = vars(n);
        new QuineMcCluskey(v)
                .fillTableWith(new BoolTableByteArray(tab))
                .simplify(ps);

        ArrayList<ArrayList<TableRow>> solutions = ps.getAllSolutions();
        if (solutions != null) {

            for (ArrayList<TableRow> sol : solutions) {
                Expression e = QuineMcCluskey.addAnd(null, sol, v);
                ContextFiller context = new ContextFiller(v);
                for (int i = 0; i < tab.length; i++) {
                    if (tab[i] <= 1) {
                        assertEquals(tab[i] == 1, e.calculate(context.setContextTo(i)));

                    }
                }
            }
        }
    }


}
