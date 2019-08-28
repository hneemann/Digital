/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics;

import junit.framework.TestCase;

import java.io.IOException;

/**
 */
public class GraphicSVGLaTeXTest extends TestCase {
    public void testFormatText() throws Exception {
        GraphicSVG.TextStyle gs = new TextFormatLaTeX(false);

        assertEquals("$Z_0$", gs.format("$Z_0$", Style.NORMAL));
        assertEquals("$Z_{in}$", gs.format("$Z_{in}$", Style.NORMAL));
        assertEquals("$Z_0$", gs.format("Z_0", Style.NORMAL));
        assertEquals("\\&amp;", gs.format("&", Style.NORMAL));
        assertEquals("$\\geq\\!\\!{}$1", gs.format("\u22651", Style.NORMAL));
        assertEquals("$\\geq\\!\\!{}1$", gs.format("$\u22651$", Style.NORMAL));
        assertEquals("$\\overline{\\mbox{Q}}$", gs.format("~Q", Style.NORMAL));
        assertEquals("$\\overline{Q}$", gs.format("$~Q$", Style.NORMAL));
        assertEquals("\\textless{}a\\textgreater{}", gs.format("<a>", Style.NORMAL));
        assertEquals("Grün", gs.format("Grün", Style.NORMAL));


        assertEquals("{\\scriptsize Grün}", gs.format("Grün", Style.SHAPE_PIN));
        assertEquals("{\\scriptsize $Z_0$}", gs.format("Z_0", Style.SHAPE_PIN));
        assertEquals("{\\tiny $Z_0$}", gs.format("Z_0", Style.SHAPE_SPLITTER));
        assertEquals("{\\tiny $Z_0$}", gs.format("Z_0", Style.WIRE_BITS));
    }

    public void testCleanLabel() throws IOException {
        check("$Z_0$", "$Z_0$");
        check("$Z_{in}$", "$Z_{in}$");
        check("Z_0", "$Z_0$");
        check("$Z_0^n$", "$Z^n_0$");
    }

    private void check(String orig, String LaTeX) throws IOException {
        GraphicSVG.TextStyle gs = new TextFormatLaTeX(false);
        assertEquals(LaTeX, gs.format(orig, Style.NORMAL));
    }
}
