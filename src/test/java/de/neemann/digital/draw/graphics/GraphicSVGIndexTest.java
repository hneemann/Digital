/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics;

import junit.framework.TestCase;

/**
 */
public class GraphicSVGIndexTest extends TestCase {
    public void testFormatText() throws Exception {
        GraphicSVGIndex gs = new GraphicSVGIndex(System.out, null, 30);
        gs.setBoundingBox(new Vector(0, 0), new Vector(30, 30));

        assertEquals("Z<tspan style=\"font-size:80%;baseline-shift:sub\">0</tspan>", gs.formatText("Z_0", Style.NORMAL));
        assertEquals("&lt;a&gt;", gs.formatText("<a>", Style.NORMAL));
        assertEquals("\u00ACZ", gs.formatText("~Z", Style.NORMAL));
    }

}
