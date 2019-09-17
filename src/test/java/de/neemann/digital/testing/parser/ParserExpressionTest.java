/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.testing.parser;

import junit.framework.TestCase;

import java.io.IOException;

/**
 * Created by Helmut.Neemann on 02.12.2016.
 */
public class ParserExpressionTest extends TestCase {

    public void testParseExpression() throws Exception {
        assertEquals(7, new Parser("2+5").getValue());
        assertEquals(7, new Parser("9-2").getValue());
        assertEquals(6, new Parser("2*n").getValue(new Context().setVar("n", 3)));
        assertEquals(7, new Parser("2*n+1").getValue(new Context().setVar("n", 3)));
        assertEquals(7, new Parser("1+2*n").getValue(new Context().setVar("n", 3)));
        assertEquals(8, new Parser("2*(1+n)").getValue(new Context().setVar("n", 3)));
        assertEquals(4, new Parser("2*(n-1)").getValue(new Context().setVar("n", 3)));
        assertEquals(2, new Parser("(2*n)/3").getValue(new Context().setVar("n", 3)));
        assertEquals(-1, new Parser("-1").getValue());
        assertEquals(-2, new Parser("-1-1").getValue());

        assertEquals(7, new Parser("7%8").getValue());
        assertEquals(0, new Parser("8%8").getValue());
        assertEquals(1, new Parser("9%8").getValue());

        assertEquals(8, new Parser("1<<3").getValue());
        assertEquals(2, new Parser("8>>2").getValue());

        assertEquals(1, new Parser("1<3").getValue());
        assertEquals(0, new Parser("3<1").getValue());
        assertEquals(1, new Parser("3>1").getValue());
        assertEquals(0, new Parser("1>3").getValue());
        assertEquals(0, new Parser("1=3").getValue());
        assertEquals(1, new Parser("3=3").getValue());
        assertEquals(1, new Parser("1!=2").getValue());
        assertEquals(0, new Parser("2!=2").getValue());

        assertEquals(1, new Parser("1<=3").getValue());
        assertEquals(1, new Parser("3<=3").getValue());
        assertEquals(0, new Parser("4<=3").getValue());
        assertEquals(1, new Parser("3>=1").getValue());
        assertEquals(1, new Parser("3>=3").getValue());
        assertEquals(0, new Parser("3>=4").getValue());

        assertEquals(7, new Parser("3|4").getValue());
        assertEquals(2, new Parser("7&2").getValue());
        assertEquals(5, new Parser("7^2").getValue());

        assertEquals(-1, new Parser("~0").getValue());
        assertEquals(-2, new Parser("~1").getValue());

        assertEquals(1, new Parser("!0").getValue());
        assertEquals(0, new Parser("!1").getValue());
        assertEquals(0, new Parser("!2").getValue());

        assertEquals(1, new Parser("(n>>8)*(n&255)").getValue(new Context().setVar("n", 257)));

        assertEquals(0x11, new Parser("0x10+1").getValue());
        assertEquals(0b11, new Parser("0b10+1").getValue());

        assertEquals(6, new Parser("a*b").getValue(
                new Context()
                        .setVar("a", 2)
                        .setVar("b", 3)));

        assertEquals(-1, new Parser("signExt(4,15)").getValue());
        assertEquals(-2, new Parser("signExt(4,14)").getValue());
        assertEquals(1, new Parser("signExt(4,1)").getValue());
        assertEquals(2, new Parser("signExt(4,2)").getValue());

        assertEquals(8, new Parser("ite(1=1,8,0)").getValue());
        assertEquals(2, new Parser("ite(1=0,8,2)").getValue());
        assertEquals(2, new Parser("ite(1<1,8,2)").getValue());
        assertEquals(8, new Parser("ite(1>0,8,2)").getValue());

    }

    public void testVarNotFound() throws IOException {
        try {
            new Parser("n*3").getValue();
            fail();
        } catch (ParserException e) {
            assertTrue(true);
        }
    }

    public void testInvalidExpressionClose() throws IOException {
        try {
            new Parser("n*3)").getValue(new Context().setVar("n", 2));
            fail();
        } catch (ParserException e) {
            assertTrue(true);
        }
    }
}
