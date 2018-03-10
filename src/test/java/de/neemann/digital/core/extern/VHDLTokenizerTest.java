/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.extern;

import junit.framework.TestCase;

import java.io.IOException;
import java.io.StringReader;

public class VHDLTokenizerTest extends TestCase {

    public void testTokenizer() throws IOException, VHDLTokenizer.TokenizerException {
        VHDLTokenizer tok = new VHDLTokenizer(new StringReader("aa-bb"));
        assertEquals(VHDLTokenizer.Token.IDENT, tok.next());
        assertEquals("aa", tok.value());
        assertEquals(VHDLTokenizer.Token.UNKNOWN, tok.next());
        assertEquals("-", tok.value());
        assertEquals(VHDLTokenizer.Token.IDENT, tok.next());
        assertEquals("bb", tok.value());
    }

    public void testTokenizerComment() throws IOException, VHDLTokenizer.TokenizerException {
        VHDLTokenizer tok = new VHDLTokenizer(new StringReader("aa--gfgfg\nbb"));
        assertEquals(VHDLTokenizer.Token.IDENT, tok.next());
        assertEquals("aa", tok.value());
        assertEquals(VHDLTokenizer.Token.IDENT, tok.next());
        assertEquals("bb", tok.value());
    }

    public void testTokenizerBracket() throws IOException, VHDLTokenizer.TokenizerException {
        VHDLTokenizer tok = new VHDLTokenizer(new StringReader("(aa)"));
        assertEquals(VHDLTokenizer.Token.OPEN, tok.next());
        assertEquals(VHDLTokenizer.Token.IDENT, tok.next());
        assertEquals("aa", tok.value());
        assertEquals(VHDLTokenizer.Token.CLOSE, tok.next());
    }

}