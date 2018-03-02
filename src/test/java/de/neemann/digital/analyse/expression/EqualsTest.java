/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.expression;

import junit.framework.TestCase;

import static de.neemann.digital.analyse.expression.Not.not;
import static de.neemann.digital.analyse.expression.Operation.and;
import static de.neemann.digital.analyse.expression.Operation.or;
import static de.neemann.digital.analyse.expression.Variable.v;

/**
 */
public class EqualsTest extends TestCase {

    public void testConst() throws Exception {
        Equals eq = new Equals(Constant.ONE, Constant.ONE);
        assertTrue(eq.isEqual());
        eq = new Equals(Constant.ONE, Constant.ZERO);
        assertFalse(eq.isEqual());
    }

    public void testNumVars() throws Exception {
        Equals eq = new Equals(v("a"), Constant.ONE);
        assertFalse(eq.isEqual());
    }

    public void testNotSameVars() throws Exception {
        Equals eq = new Equals(v("a"), v("b"));
        assertFalse(eq.isEqual());
    }

    public void testSameVars() throws Exception {
        Equals eq = new Equals(v("a"), v("a"));
        assertTrue(eq.isEqual());
    }

    public void testTwoVars() throws Exception {
        Equals eq = new Equals(or(v("a"), v("b")), and(v("a"),v("b")));
        assertFalse(eq.isEqual());
    }

    public void testTwoVars2() throws Exception {
        Equals eq = new Equals(or(v("a"), v("b")), not(and(not(v("a")),not(v("b")))));
        assertTrue(eq.isEqual());
    }

}
