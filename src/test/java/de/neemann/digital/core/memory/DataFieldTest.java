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

    public void testGetMinimized() throws Exception {
        DataField data = new DataField(100);
        data.setData(9, 1);
        data = data.getMinimized();
        assertEquals(1, data.getDataWord(9));
        data = data.getMinimized();
        assertEquals(1, data.getDataWord(9));
    }

    public void testGrow() throws Exception {
        DataField data = new DataField(100);
        data.setData(9, 1);
        data = data.getMinimized();
        assertEquals(1, data.getDataWord(9));
        data.setData(30, 1);
        assertEquals(1, data.getDataWord(30));
    }

    public void testLoad() throws Exception {
        String data = "v2.0 raw\n0\n10\nAA\nFF";
        DataField df = new DataField(new StringReader(data));
        assertEquals(4, df.size());
        assertEquals(0x00, df.getDataWord(0));
        assertEquals(0x10, df.getDataWord(1));
        assertEquals(0xAA, df.getDataWord(2));
        assertEquals(0xFF, df.getDataWord(3));
    }

    public void testLoad64Bit() throws Exception {
        String data = "v2.0 raw\n8000000000000000\n10\nAA\nFF";
        DataField df = new DataField(new StringReader(data));
        assertEquals(4, df.size());
        assertEquals(0x8000000000000000L, df.getDataWord(0));
        assertEquals(0x10, df.getDataWord(1));
        assertEquals(0xAA, df.getDataWord(2));
        assertEquals(0xFF, df.getDataWord(3));
    }

    public void testLoadComments() throws Exception {
        String data = "v2.0 raw\n#test1 \n 0 \n#test1\n  #  test2\n10  # test3\n\n\nAA\nFF #test";
        DataField df = new DataField(new StringReader(data));
        assertEquals(4, df.size());
        assertEquals(0x00, df.getDataWord(0));
        assertEquals(0x10, df.getDataWord(1));
        assertEquals(0xAA, df.getDataWord(2));
        assertEquals(0xFF, df.getDataWord(3));
    }
}
