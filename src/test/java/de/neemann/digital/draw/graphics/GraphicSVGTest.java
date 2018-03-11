/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics;

import junit.framework.TestCase;

/**
 */
public class GraphicSVGTest extends TestCase {
    public void testFormatText() throws Exception {
        GraphicSVG gs = new GraphicSVG(System.out, null, 30);
        gs.setBoundingBox(new Vector(0, 0), new Vector(30, 30));

        assertEquals("Z0", gs.formatText("Z0", Style.NORMAL));
        assertEquals("&lt;a&gt;", gs.formatText("<a>", Style.NORMAL));
    }

}
