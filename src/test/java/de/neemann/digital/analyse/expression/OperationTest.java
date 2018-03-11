/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.expression;

import junit.framework.TestCase;

import static de.neemann.digital.analyse.expression.Not.not;
import static de.neemann.digital.analyse.expression.Operation.*;
import static de.neemann.digital.analyse.expression.Variable.v;

/**
 */
public class OperationTest extends TestCase {

    public void testOr() throws Exception {
        Variable a = v("a");
        Variable b = v("b");
        Variable c = v("c");

        Expression i = or(a);
        assertTrue(i instanceof Variable);
        i = or(a, b);
        assertTrue(i instanceof Operation.Or);
        assertEquals(2, ((Operation.Or) i).getExpressions().size());
        i = or(c, i);
        assertEquals(3, ((Operation.Or) i).getExpressions().size());

        i = or(and(a, b), c);
        assertTrue(i instanceof Operation.Or);
        assertEquals(2, ((Operation.Or) i).getExpressions().size());

        i = or(not(a));
        assertTrue(i instanceof Not);
    }

    public void testAnd() throws Exception {
        Variable a = v("a");
        Variable b = v("b");
        Variable c = v("c");

        assertTrue(and(a) instanceof Variable);
        Expression i = and(a, b);
        assertTrue(i instanceof Operation.And);
        assertEquals(2, ((Operation.And) i).getExpressions().size());
        i = and(c, i);
        assertEquals(3, ((Operation.And) i).getExpressions().size());

        i = and(or(a, b), c);
        assertTrue(i instanceof Operation.And);
        assertEquals(2, ((Operation.And) i).getExpressions().size());

        i = and(not(a));
        assertTrue(i instanceof Not);
    }

    public void testXOr() throws Exception {
        Variable a = v("a");
        Variable b = v("b");

        Expression i = xor(a,b);
        assertTrue(i instanceof Operation.XOr);
        assertEquals(2, ((Operation.XOr) i).getExpressions().size());

        assertEquals(false, ((Operation)i).calc(false,false));
        assertEquals(true, ((Operation)i).calc(true,false));
        assertEquals(true, ((Operation)i).calc(false,true));
        assertEquals(false, ((Operation)i).calc(true,true));
    }

}
