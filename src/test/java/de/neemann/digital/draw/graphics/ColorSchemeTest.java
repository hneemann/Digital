/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics;

import junit.framework.TestCase;

public class ColorSchemeTest extends TestCase {

    public void testCompleteness() {
        ColorScheme map = ColorScheme.COLOR_SCHEME.getDefault().getScheme();
        for (ColorKey ck : ColorKey.values())
            assertNotNull(map.getColor(ck));
    }

}