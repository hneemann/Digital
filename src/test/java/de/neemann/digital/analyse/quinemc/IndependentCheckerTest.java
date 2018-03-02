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

import static de.neemann.digital.analyse.expression.Operation.and;

/**
 */
public class IndependentCheckerTest extends TestCase {
    private Variable a = new Variable("A");
    private Variable b = new Variable("B");
    private Variable c = new Variable("C");
    private Variable d = new Variable("D");

    public void testSimple() {
        Expression ex = and(a, b, c);

        ContextFiller cf = new ContextFiller(a, b, c, d);

        BoolTableExpression bte = new BoolTableExpression(ex, cf);

        IndependentChecker ic = new IndependentChecker(bte);
        assertEquals(4, ic.getVars());

        assertFalse(ic.isIndependentFrom(0));
        assertFalse(ic.isIndependentFrom(1));
        assertFalse(ic.isIndependentFrom(2));
        assertTrue(ic.isIndependentFrom(3));
    }

    public void testSimple2() {
        Expression ex = and(a, c, d);

        ContextFiller cf = new ContextFiller(a, b, c, d);

        BoolTableExpression bte = new BoolTableExpression(ex, cf);

        IndependentChecker ic = new IndependentChecker(bte);
        assertEquals(4, ic.getVars());

        assertFalse(ic.isIndependentFrom(0));
        assertTrue(ic.isIndependentFrom(1));
        assertFalse(ic.isIndependentFrom(2));
        assertFalse(ic.isIndependentFrom(3));
    }


    public void testRemoveVar() {
        Expression ex = and(a, b, c);
        ContextFiller cf = new ContextFiller(a, b, c, d);
        BoolTableExpression bte = new BoolTableExpression(ex, cf);
        IndependentChecker ic = new IndependentChecker(bte);
        BoolTable btr = ic.removeVar(3);
        assertEquals(8, btr.size());
        for (int i = 0; i < 7; i++)
            assertEquals(ThreeStateValue.zero, btr.get(i));
        assertEquals(ThreeStateValue.one, btr.get(7));
    }

    public void testRemoveVar2() {
        Expression ex = and(a, c, d);
        ContextFiller cf = new ContextFiller(a, b, c, d);
        BoolTableExpression bte = new BoolTableExpression(ex, cf);
        IndependentChecker ic = new IndependentChecker(bte);
        BoolTable btr = ic.removeVar(1);
        assertEquals(8, btr.size());
        for (int i = 0; i < 7; i++)
            assertEquals(ThreeStateValue.zero, btr.get(i));
        assertEquals(ThreeStateValue.one, btr.get(7));
    }

}
