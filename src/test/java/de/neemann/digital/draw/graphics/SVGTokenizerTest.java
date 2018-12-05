/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics;

import junit.framework.TestCase;

public class SVGTokenizerTest extends TestCase {

    public void testSimple() throws SVGTokenizer.TokenizerException {
        SVGTokenizer t = new SVGTokenizer("m 1,2");
        assertEquals("m", t.readCommand());
        assertEquals(1.0, t.readFloat(), 1e-6);
        assertEquals(2.0, t.readFloat(), 1e-6);
        assertTrue(t.isEOF());
        assertTrue(t.isEOF());
    }

    public void testSimpleExp() throws SVGTokenizer.TokenizerException {
        SVGTokenizer t = new SVGTokenizer("1e1");
        assertEquals(10, t.readFloat(), 1e-6);
        assertTrue(t.isEOF());
    }

    public void testSimpleExp2() throws SVGTokenizer.TokenizerException {
        SVGTokenizer t = new SVGTokenizer("1em");
        assertEquals(1, t.readFloat(), 1e-6);
        assertEquals("em", t.readCommand());
        assertTrue(t.isEOF());
    }

    public void testSimpleExp3() throws SVGTokenizer.TokenizerException {
        SVGTokenizer t = new SVGTokenizer("1e-1em");
        assertEquals(0.1, t.readFloat(), 1e-6);
        assertEquals("em", t.readCommand());
        assertTrue(t.isEOF());
    }

    public void testSimpleExp4() throws SVGTokenizer.TokenizerException {
        SVGTokenizer t = new SVGTokenizer("1e+1em");
        assertEquals(10, t.readFloat(), 1e-6);
        assertEquals("em", t.readCommand());
        assertTrue(t.isEOF());
    }

    public void testSimpleExpSign() throws SVGTokenizer.TokenizerException {
        SVGTokenizer t = new SVGTokenizer("1e-1");
        assertEquals(0.1, t.readFloat(), 1e-6);
        assertTrue(t.isEOF());
    }

    public void testRemaining() throws SVGTokenizer.TokenizerException {
        SVGTokenizer t = new SVGTokenizer("test:World(-)");
        assertEquals("test", t.readCommand());
        t.expect(':');
        assertEquals("World(-)", t.remaining());
        assertTrue(t.isEOF());
    }

    public void testReadto() throws SVGTokenizer.TokenizerException {
        SVGTokenizer t = new SVGTokenizer("test:World(-);");
        assertEquals("test", t.readCommand());
        t.expect(':');
        assertEquals("World(-)", t.readTo(';'));
        assertTrue(t.isEOF());
    }

    public void testReadtoEnd() throws SVGTokenizer.TokenizerException {
        SVGTokenizer t = new SVGTokenizer("test:World(-)");
        assertEquals("test", t.readCommand());
        t.expect(':');
        assertEquals("World(-)", t.readTo(';'));
        assertTrue(t.isEOF());
    }

    public void testReadtoEndNested() throws SVGTokenizer.TokenizerException {
        SVGTokenizer t = new SVGTokenizer("test:World(;)");
        assertEquals("test", t.readCommand());
        t.expect(':');
        assertEquals("World(;)", t.readTo(';'));
        assertTrue(t.isEOF());
    }

    public void testReadtoEndNested2() throws SVGTokenizer.TokenizerException {
        SVGTokenizer t = new SVGTokenizer("test:World(;);test:World");
        assertEquals("test", t.readCommand());
        t.expect(':');
        assertEquals("World(;)", t.readTo(';'));
        assertEquals("test", t.readCommand());
        t.expect(':');
        assertEquals("World", t.readTo(';'));
        assertTrue(t.isEOF());
    }

}