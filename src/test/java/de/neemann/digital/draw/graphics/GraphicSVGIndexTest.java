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
    public void testFormatText() {
        GraphicSVG.TextStyle gs = new TextFormatSVG();

        assertEquals("Z<tspan style=\"font-size:80%;baseline-shift:sub;\">0</tspan>", gs.format("Z_0", Style.NORMAL));
        assertEquals("&lt;a&gt;", gs.format("<a>", Style.NORMAL));
        assertEquals("<tspan style=\"text-decoration:overline;\">Z</tspan>", gs.format("~Z", Style.NORMAL));
    }

}
