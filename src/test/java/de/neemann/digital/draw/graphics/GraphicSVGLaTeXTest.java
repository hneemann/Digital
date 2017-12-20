package de.neemann.digital.draw.graphics;

import junit.framework.TestCase;

/**
 * @author hneemann
 */
public class GraphicSVGLaTeXTest extends TestCase {
    public void testFormatText() throws Exception {
        GraphicSVGLaTeX gs = new GraphicSVGLaTeX(System.out, null, 30);
        gs.setBoundingBox(new Vector(0, 0), new Vector(30, 30));

        assertEquals("Z$_{0}$", gs.formatText("Z_0", Style.NORMAL.getFontSize()));
        assertEquals("\\&amp;", gs.formatText("&", Style.NORMAL.getFontSize()));
        assertEquals("$\\geq\\!\\!{}$1", gs.formatText("\u22651", Style.NORMAL.getFontSize()));
        assertEquals("$\\geq\\!\\!{}1$", gs.formatText("$\u22651$", Style.NORMAL.getFontSize()));
        assertEquals("$\\neg{}$Q", gs.formatText("~Q", Style.NORMAL.getFontSize()));
        assertEquals("$\\neg{}Q$", gs.formatText("$~Q$", Style.NORMAL.getFontSize()));
        assertEquals("\\textless{}a\\textgreater{}", gs.formatText("<a>", Style.NORMAL.getFontSize()));
        assertEquals("Grün", gs.formatText("Grün", Style.NORMAL.getFontSize()));


        assertEquals("{\\footnotesize Grün}", gs.formatText("Grün", Style.SHAPE_PIN.getFontSize()));
        assertEquals("{\\footnotesize Z$_{0}$}", gs.formatText("Z_0", Style.SHAPE_PIN.getFontSize()));
        assertEquals("{\\scriptsize Z$_{0}$}", gs.formatText("Z_0", 14));
        assertEquals("{\\tiny Z$_{0}$}", gs.formatText("Z_0", Style.WIRE_BITS.getFontSize()));
    }

}