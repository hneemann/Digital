package de.neemann.digital.draw.graphics;

import junit.framework.TestCase;

/**
 * @author hneemann
 */
public class GraphicSVGTest extends TestCase {
    public void testFormatText() throws Exception {
        GraphicSVG gs = new GraphicSVG(System.out, null, 30);
        gs.setBoundingBox(new Vector(0, 0), new Vector(30, 30));

        assertEquals("Z0", gs.formatText("Z0", 0));
        assertEquals("&lt;a&gt;", gs.formatText("<a>", 0));
    }

}