/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.expression;

import junit.framework.TestCase;

import java.util.ArrayList;

import static de.neemann.digital.analyse.expression.Variable.vars;

/**
 */
public class VariableTest extends TestCase {

    public void testVars() throws Exception {
        ArrayList<Variable> v = vars(5);
        assertEquals(5, v.size());
        assertEquals("A", v.get(0).getIdentifier());
        assertEquals("B", v.get(1).getIdentifier());
        assertEquals("C", v.get(2).getIdentifier());
        assertEquals("D", v.get(3).getIdentifier());
        assertEquals("E", v.get(4).getIdentifier());
    }
}
