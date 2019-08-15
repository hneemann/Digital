/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.lang;

import de.neemann.gui.language.Language;
import junit.framework.TestCase;

public class TestMultiligual extends TestCase {

    public void testSimple() {
        assertEquals("World", Lang.evalMultilingualContent("World", new Language("de")));
        assertEquals("Welt", Lang.evalMultilingualContent("World  {{DE Welt}}", new Language("de")));
        assertEquals("Monde", Lang.evalMultilingualContent("World  {{DE Welt}} {{fr Monde}}", new Language("fr")));
        assertEquals("Welt", Lang.evalMultilingualContent("World \n {{DE Welt}} \n{{fr Monde}}", new Language("de")));
        assertEquals("World", Lang.evalMultilingualContent("World  {{DE Welt}} {{fr Monde}}", new Language("en")));
    }

    public void testErrors() {
        assertEquals("", Lang.evalMultilingualContent("World {{de}}", new Language("de")));
        assertEquals("World", Lang.evalMultilingualContent("World {{de }", new Language("de")));
        assertEquals("World", Lang.evalMultilingualContent("World {{de", new Language("de")));
        assertEquals("World", Lang.evalMultilingualContent("World {{d", new Language("de")));
        assertEquals("World", Lang.evalMultilingualContent("World {{", new Language("de")));
        assertEquals("World {", Lang.evalMultilingualContent("World {", new Language("de")));

        assertEquals("World", Lang.evalMultilingualContent("World {{de}}", new Language("fr")));
        assertEquals("World", Lang.evalMultilingualContent("World {{de }", new Language("fr")));
        assertEquals("World", Lang.evalMultilingualContent("World {{de", new Language("fr")));
    }

    public void testNesting() {
        final String text = "explains ~{s_1^{n+1}} {{de erklärt ~{s_1^{n+1}}}} {{es explica ~{s_1^{n+1}}}}";
        assertEquals("explains ~{s_1^{n+1}}", Lang.evalMultilingualContent(text, new Language("en")));
        assertEquals("erklärt ~{s_1^{n+1}}", Lang.evalMultilingualContent(text, new Language("de")));
        assertEquals("explica ~{s_1^{n+1}}", Lang.evalMultilingualContent(text, new Language("es")));
    }

}
