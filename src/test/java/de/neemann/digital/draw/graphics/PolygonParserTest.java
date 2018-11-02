/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics;

import junit.framework.TestCase;

public class PolygonParserTest extends TestCase {

    public void testSimple() {
        PolygonParser pp = new PolygonParser("m 1,2");

        assertEquals(PolygonParser.Token.COMMAND, pp.next());
        assertEquals('m', pp.getCommand());

        assertEquals(PolygonParser.Token.NUMBER, pp.next());
        assertEquals(1.0, pp.getValue(), 1e-6);

        assertEquals(PolygonParser.Token.NUMBER, pp.next());
        assertEquals(2.0, pp.getValue(), 1e-6);

        assertEquals(PolygonParser.Token.EOF, pp.next());
        assertEquals(PolygonParser.Token.EOF, pp.next());
    }

    public void testSimpleExp() {
        PolygonParser pp = new PolygonParser("1e1");
        assertEquals(PolygonParser.Token.NUMBER, pp.next());
        assertEquals(10, pp.getValue(), 1e-6);
        assertEquals(PolygonParser.Token.EOF, pp.next());
    }

    public void testSimpleExpSign() {
        PolygonParser pp = new PolygonParser("1e-1");
        assertEquals(PolygonParser.Token.NUMBER, pp.next());
        assertEquals(0.1, pp.getValue(), 1e-6);
        assertEquals(PolygonParser.Token.EOF, pp.next());
    }

}