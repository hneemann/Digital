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
        GraphicSVG.TextStyle gs = new TextFormatSVG();

        assertEquals("Z0", gs.format("Z0", Style.NORMAL));
        assertEquals("&lt;a&gt;", gs.format("<a>", Style.NORMAL));
    }

}
