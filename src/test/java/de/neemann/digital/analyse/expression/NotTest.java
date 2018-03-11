/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.expression;

import junit.framework.TestCase;

import static de.neemann.digital.analyse.expression.Not.not;
import static de.neemann.digital.analyse.expression.Variable.v;

/**
 */
public class NotTest extends TestCase {

    public void testNot() throws Exception {
        assertEquals(Constant.ONE, not(Constant.ZERO));
        assertEquals(Constant.ZERO, not(Constant.ONE));

        Variable a = v("a");
        assertEquals(a, not(not(a)));

        //assertEquals("¬a", not(a).toString());
    }
}
