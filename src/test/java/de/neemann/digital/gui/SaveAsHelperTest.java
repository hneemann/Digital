/*
 * Copyright (c) 2019 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui;

import junit.framework.TestCase;

import java.io.File;

public class SaveAsHelperTest extends TestCase {

    public void testCheckSuffix() {
        assertEquals("test.dig", SaveAsHelper.checkSuffix(new File("test."), "dig").getName());
        assertEquals("test.dig", SaveAsHelper.checkSuffix(new File("test"), "dig").getName());

        assertEquals("test.dig", SaveAsHelper.checkSuffix(new File("test.dig"), "dig").getName());
        assertEquals("test.dig", SaveAsHelper.checkSuffix(new File("test.svg"), "dig").getName());
        assertEquals("test.main.dig", SaveAsHelper.checkSuffix(new File("test.main"), "dig").getName());
        assertEquals("test.main.dig", SaveAsHelper.checkSuffix(new File("test.main.dig"), "dig").getName());
        assertEquals("test.mai.dig", SaveAsHelper.checkSuffix(new File("test.mai"), "dig").getName());

        assertEquals("test.dig", SaveAsHelper.checkSuffix(new File("test.v"), "dig").getName());
        assertEquals("test.dig", SaveAsHelper.checkSuffix(new File("test.vhdl"), "dig").getName());

    }
}