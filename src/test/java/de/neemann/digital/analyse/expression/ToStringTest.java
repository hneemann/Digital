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

/**
 */
public class ToStringTest extends TestCase {

    public void testToString() throws Exception {
        Variable D = new Variable("D");

        Variable Q0 = new Variable("Q0");
        Variable Q1 = new Variable("Q1");
        Variable Q2 = new Variable("Q2");

        //Q1.d = !D & !Q1 & Q0 # !D & Q1 & !Q0 # D & !Q1 & !Q0 # D & Q1 & Q0;
        Expression Q1d = or(and(not(D), not(Q1), Q0), and(not(D), Q1, not(Q0)), and(D, not(Q1), not(Q0)), and(D, Q1, Q0));

        assertEquals("or(and(not(D),Q0,not(Q1)),and(not(D),not(Q0),Q1),and(D,not(Q0),not(Q1)),and(D,Q0,Q1))", Q1d.toString());
    }
}
