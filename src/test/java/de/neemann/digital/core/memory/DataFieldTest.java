/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.memory;

import junit.framework.TestCase;

import java.io.StringReader;

/**
 */
public class DataFieldTest extends TestCase {

    public void testGetMinimized() {
        DataField data = new DataField(100);
        data.setData(9, 1);
        assertEquals(10, data.trim());
        assertEquals(1, data.getDataWord(9));
        assertEquals(10, data.trim());
        assertEquals(1, data.getDataWord(9));
    }

    public void testGrow() {
        DataField data = new DataField(100);
        data.setData(9, 1);
        data.trim();
        assertEquals(1, data.getDataWord(9));
        data.setData(30, 1);
        assertEquals(1, data.getDataWord(30));
    }

    public void testGrow2() {
        DataField data = new DataField(0);
        data.setData(0, 1);
        assertEquals(1, data.getDataWord(0));
        assertEquals(0, data.getDataWord(1));
    }
}
