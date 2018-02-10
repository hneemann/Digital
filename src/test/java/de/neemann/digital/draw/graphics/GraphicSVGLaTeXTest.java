package de.neemann.digital.draw.graphics;

import de.neemann.digital.core.element.ElementAttributes;
import junit.framework.TestCase;

import java.io.IOException;

/**
 * @author hneemann
 */
public class GraphicSVGLaTeXTest extends TestCase {
    public void testFormatText() throws Exception {
        GraphicSVGLaTeX gs = new GraphicSVGLaTeX(System.out, null, 30);

        assertEquals("$Z_0$", gs.formatText("$Z_0$", Style.NORMAL.getFontSize()));
        assertEquals("$Z_{in}$", gs.formatText("$Z_{in}$", Style.NORMAL.getFontSize()));
        assertEquals("Z$_{0}$", gs.formatText("Z_0", Style.NORMAL.getFontSize()));
        assertEquals("\\&amp;", gs.formatText("&", Style.NORMAL.getFontSize()));
        assertEquals("$\\geq\\!\\!{}$1", gs.formatText("\u22651", Style.NORMAL.getFontSize()));
        assertEquals("$\\geq\\!\\!{}1$", gs.formatText("$\u22651$", Style.NORMAL.getFontSize()));
        assertEquals("$\\neg{}$Q", gs.formatText("~Q", Style.NORMAL.getFontSize()));
        assertEquals("$\\neg{}Q$", gs.formatText("$~Q$", Style.NORMAL.getFontSize()));
        assertEquals("\\textless{}a\\textgreater{}", gs.formatText("<a>", Style.NORMAL.getFontSize()));
        assertEquals("Grün", gs.formatText("Grün", Style.NORMAL.getFontSize()));


        assertEquals("{\\scriptsize Grün}", gs.formatText("Grün", Style.SHAPE_PIN.getFontSize()));
        assertEquals("{\\scriptsize Z$_{0}$}", gs.formatText("Z_0", Style.SHAPE_PIN.getFontSize()));
        assertEquals("{\\tiny Z$_{0}$}", gs.formatText("Z_0", 14));
        assertEquals("{\\tiny Z$_{0}$}", gs.formatText("Z_0", Style.WIRE_BITS.getFontSize()));
    }

    public void testCleanLabel() throws IOException {
        check("$Z_0$", "Z_0", "$Z_0$");
        check("$Z_{in}$", "Z_in", "$Z_{in}$");
        check("Z_0", "Z_0", "Z$_{0}$");
        check("$Z_0^n$", "Z_0n", "$Z_0^n$");
    }

    private void check(String orig, String clean, String LaTeX) throws IOException {
        GraphicSVGLaTeX gs = new GraphicSVGLaTeX(System.out, null, 30);
        assertEquals(LaTeX, gs.formatText(orig, Style.NORMAL.getFontSize()));
        assertEquals(clean, ElementAttributes.cleanLabel(orig));
    }
}