/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics;

import de.neemann.digital.core.element.ElementAttributes;
import junit.framework.TestCase;

import java.io.IOException;

/**
 */
public class GraphicSVGLaTeXTest extends TestCase {
    public void testFormatText() throws Exception {
        GraphicSVGLaTeX gs = new GraphicSVGLaTeX(System.out, null, 30);

        assertEquals("$Z_0$", gs.formatText("$Z_0$", Style.NORMAL));
        assertEquals("$Z_{in}$", gs.formatText("$Z_{in}$", Style.NORMAL));
        assertEquals("$Z_0$", gs.formatText("Z_0", Style.NORMAL));
        assertEquals("\\&amp;", gs.formatText("&", Style.NORMAL));
        assertEquals("$\\geq\\!\\!{}$1", gs.formatText("\u22651", Style.NORMAL));
        assertEquals("$\\geq\\!\\!{}1$", gs.formatText("$\u22651$", Style.NORMAL));
        assertEquals("$\\overline{\\mbox{Q}}$", gs.formatText("~Q", Style.NORMAL));
        assertEquals("$\\overline{Q}$", gs.formatText("$~Q$", Style.NORMAL));
        assertEquals("\\textless{}a\\textgreater{}", gs.formatText("<a>", Style.NORMAL));
        assertEquals("Grün", gs.formatText("Grün", Style.NORMAL));


        assertEquals("{\\scriptsize Grün}", gs.formatText("Grün", Style.SHAPE_PIN));
        assertEquals("{\\scriptsize $Z_0$}", gs.formatText("Z_0", Style.SHAPE_PIN));
        assertEquals("{\\tiny $Z_0$}", gs.formatText("Z_0", Style.SHAPE_SPLITTER));
        assertEquals("{\\tiny $Z_0$}", gs.formatText("Z_0", Style.WIRE_BITS));
    }

    public void testCleanLabel() throws IOException {
        check("$Z_0$", "Z_0", "$Z_0$");
        check("$Z_{in}$", "Z_in", "$Z_{in}$");
        check("Z_0", "Z_0", "$Z_0$");
        check("$Z_0^n$", "Z_0n", "$Z^n_0$");
    }

    private void check(String orig, String clean, String LaTeX) throws IOException {
        GraphicSVGLaTeX gs = new GraphicSVGLaTeX(System.out, null, 30);
        assertEquals(LaTeX, gs.formatText(orig, Style.NORMAL));
        assertEquals(clean, ElementAttributes.cleanLabel(orig));
    }
}
