/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.builder.jedec;

import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.Variable;
import junit.framework.TestCase;

import static de.neemann.digital.analyse.expression.Not.not;
import static de.neemann.digital.analyse.expression.Operation.and;
import static de.neemann.digital.analyse.expression.Operation.or;

/**
 */
public class FuseMapFillerTest extends TestCase {
    public void testFillExpression() throws Exception {

        FuseMap fuseMap = new FuseMap(16);

        Variable a = new Variable("A");
        Variable b = new Variable("B");
        Variable c = new Variable("C");
        Variable d = new Variable("D");

        Expression e = or(and(a, b, c, d), and(not(a), not(b), not(c), not(d)));

        new FuseMapFiller(fuseMap, 4)
                .addVariable(0, a)
                .addVariable(1, b)
                .addVariable(2, c)
                .addVariable(3, d)
                .fillExpression(0, e, 2);

        byte[] data = fuseMap.getFuseData();
        assertEquals(0xAA, data[0] & 0xff);
        assertEquals(0x55, data[1] & 0xff);
    }

}
