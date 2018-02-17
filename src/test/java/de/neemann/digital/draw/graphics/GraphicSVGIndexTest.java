package de.neemann.digital.draw.graphics;

import junit.framework.TestCase;

/**
 * @author hneemann
 */
public class GraphicSVGIndexTest extends TestCase {
    public void testFormatText() throws Exception {
        GraphicSVGIndex gs = new GraphicSVGIndex(System.out, null, 30);
        gs.setBoundingBox(new Vector(0, 0), new Vector(30, 30));

        assertEquals("Z<tspan style=\"font-size:80%;baseline-shift:sub\">0</tspan>", gs.formatText("Z_0", 0));
        assertEquals("&lt;a&gt;", gs.formatText("<a>", 0));
        assertEquals("¬Z", gs.formatText("~Z", 0));
    }

}