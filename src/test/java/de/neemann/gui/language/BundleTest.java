/*
 * Copyright (c) 2020 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.gui.language;

import junit.framework.TestCase;

public class BundleTest extends TestCase {

    public void testBundle() {
        Bundle b = new Bundle("lang/lang");
        assertEquals("de", b.findResource("de"));
        assertEquals("de", b.findResource("de-de"));
        assertEquals("de", b.findResource("de-at"));

        assertEquals("zh", b.findResource("zh"));
        assertEquals("zh", b.findResource("zh-cn"));
        assertEquals("zh", b.findResource("zh-hk"));
        assertEquals("zh-tw", b.findResource("zh-tw"));
    }

}