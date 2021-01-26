/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.quinemc;


import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.analyse.expression.format.FormatToExpression;
import de.neemann.digital.analyse.expression.format.FormatterException;
import junit.framework.TestCase;

import static de.neemann.digital.analyse.expression.Operation.and;
import static de.neemann.digital.analyse.expression.Operation.or;
import static de.neemann.digital.analyse.expression.Variable.v;

/**
 */
public class SimplifyTest extends TestCase {

    public void testSimplify() throws Exception, FormatterException {
        Variable a = v("a");
        Variable b = v("b");
        Expression e = or(and(a, b), a);
        Expression s = QuineMcCluskey.simplify(e);

        assertEquals("a", FormatToExpression.UNICODE.format(s));
    }

    public void testSimplify2() throws Exception, FormatterException {
        Variable a = v("a");
        Variable b = v("b");
        Expression e = and(or(a, b), a);
        Expression s = QuineMcCluskey.simplify(e);

        assertEquals("a", FormatToExpression.UNICODE.format(s));
    }

    public void testSimplify3() throws Exception {
        Variable a = v("a");
        Variable b = v("b");
        Variable c = v("c");
        Expression e = and(or(a, b), c);
        Expression s = QuineMcCluskey.simplify(e);

        assertTrue(s == e);
    }
}
