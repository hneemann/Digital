package de.neemann.digital.testing.parser;

import junit.framework.TestCase;

/**
 * Created by Helmut.Neemann on 02.12.2016.
 */
public class ParserExpressionTest extends TestCase {

    public void testParseExpression() throws Exception {
        assertEquals(7, new Parser("2+5").parseExpression().value(0));
        assertEquals(7, new Parser("9-2").parseExpression().value(0));
        assertEquals(6, new Parser("2*n").parseExpression().value(3));
        assertEquals(7, new Parser("2*n+1").parseExpression().value(3));
        assertEquals(7, new Parser("1+2*n").parseExpression().value(3));
        assertEquals(8, new Parser("2*(1+n)").parseExpression().value(3));
        assertEquals(4, new Parser("2*(n-1)").parseExpression().value(3));
        assertEquals(-1, new Parser("-1").parseExpression().value(3));
        assertEquals(-2, new Parser("-1-1").parseExpression().value(3));

        assertEquals(8, new Parser("1<<3").parseExpression().value(3));
        assertEquals(2, new Parser("8>>2").parseExpression().value(3));

        assertEquals(1, new Parser("1<3").parseExpression().value(3));
        assertEquals(0, new Parser("3<1").parseExpression().value(3));
        assertEquals(1, new Parser("3>1").parseExpression().value(3));
        assertEquals(0, new Parser("1>3").parseExpression().value(3));
        assertEquals(0, new Parser("1=3").parseExpression().value(3));
        assertEquals(1, new Parser("3=3").parseExpression().value(3));


        assertEquals(7, new Parser("3|4").parseExpression().value(3));
        assertEquals(2, new Parser("7&2").parseExpression().value(3));

        assertEquals(-1, new Parser("~0").parseExpression().value(3));

    }

}