/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.quinemc;

import de.neemann.digital.analyse.expression.ContextFiller;
import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.Variable;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

import static de.neemann.digital.analyse.expression.Operation.and;

/**
 */
public class TableReducerTest extends TestCase {
    private Variable a = new Variable("A");
    private Variable b = new Variable("B");
    private Variable c = new Variable("C");
    private Variable d = new Variable("D");


    public void testReduce() {
        List<Variable> vars=new ArrayList<>();
        vars.add(a);
        vars.add(b);
        vars.add(c);
        vars.add(d);
        Expression ex = and(a, b, c);
        ContextFiller cf = new ContextFiller(vars);
        BoolTableExpression bte = new BoolTableExpression(ex, cf);
        TableReducer tr = new TableReducer(vars, bte);

        assertTrue(tr.canReduce());

        vars = tr.getVars();
        assertEquals(3, vars.size());
        assertEquals(a, vars.get(0));
        assertEquals(b, vars.get(1));
        assertEquals(c, vars.get(2));
    }

    public void testReduce2() {
        List<Variable> vars=new ArrayList<>();
        vars.add(a);
        vars.add(b);
        vars.add(c);
        vars.add(d);
        Expression ex = and(a, c, d);
        ContextFiller cf = new ContextFiller(vars);
        BoolTableExpression bte = new BoolTableExpression(ex, cf);
        TableReducer tr = new TableReducer(vars, bte);

        assertTrue(tr.canReduce());

        vars = tr.getVars();
        assertEquals(3, vars.size());
        assertEquals(a, vars.get(0));
        assertEquals(c, vars.get(1));
        assertEquals(d, vars.get(2));
    }

}
