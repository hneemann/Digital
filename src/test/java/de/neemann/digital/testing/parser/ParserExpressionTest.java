package de.neemann.digital.testing.parser;

import junit.framework.TestCase;

/**
 * Created by Helmut.Neemann on 02.12.2016.
 */
public class ParserExpressionTest extends TestCase {

    public void testParseExpression() throws Exception {
        assertEquals(7, new Parser("2+5").parseForValue(0));
        assertEquals(7, new Parser("9-2").parseForValue(0));
        assertEquals(6, new Parser("2*n").parseForValue(3));
        assertEquals(7, new Parser("2*n+1").parseForValue(3));
        assertEquals(7, new Parser("1+2*n").parseForValue(3));
        assertEquals(8, new Parser("2*(1+n)").parseForValue(3));
        assertEquals(4, new Parser("2*(n-1)").parseForValue(3));
        assertEquals(2, new Parser("(2*n)/3").parseForValue(3));
        assertEquals(-1, new Parser("-1").parseForValue(3));
        assertEquals(-2, new Parser("-1-1").parseForValue(3));

        assertEquals(8, new Parser("1<<3").parseForValue(3));
        assertEquals(2, new Parser("8>>2").parseForValue(3));

        assertEquals(1, new Parser("1<3").parseForValue(3));
        assertEquals(0, new Parser("3<1").parseForValue(3));
        assertEquals(1, new Parser("3>1").parseForValue(3));
        assertEquals(0, new Parser("1>3").parseForValue(3));
        assertEquals(0, new Parser("1=3").parseForValue(3));
        assertEquals(1, new Parser("3=3").parseForValue(3));


        assertEquals(7, new Parser("3|4").parseForValue(3));
        assertEquals(2, new Parser("7&2").parseForValue(3));

        assertEquals(-1, new Parser("~0").parseForValue(3));

    }

}