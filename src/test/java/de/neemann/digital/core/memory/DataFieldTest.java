/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.memory;

import de.neemann.digital.core.memory.importer.DataFieldValueArray;
import de.neemann.digital.core.memory.importer.LogisimReader;
import de.neemann.digital.core.memory.importer.ValueArray;
import junit.framework.TestCase;

import java.io.*;
import java.util.Arrays;

/**
 *
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

    public void testSave() throws IOException {
        DataField data = new DataField(100);
        for (int i = 0; i < 11; i++)
            data.setData(i, i);

        StringWriter w = new StringWriter();
        data.saveTo(w);

        assertEquals("v2.0 raw\n" +
                "0\n" +
                "1\n" +
                "2\n" +
                "3\n" +
                "4\n" +
                "5\n" +
                "6\n" +
                "7\n" +
                "8\n" +
                "9\n" +
                "a\n", w.toString().replace("\r",""));
    }

    public void testSaveEmpty() throws IOException {
        DataField data = new DataField(100);

        StringWriter w = new StringWriter();
        data.saveTo(w);

        assertEquals("v2.0 raw\n", w.toString().replace("\r",""));
    }

    public void testSaveRLE() throws IOException {
        DataField data = new DataField(100);
        int pos = 0;
        for (int i = 0; i < 10; i++)
            for (int j = 0; j <= i; j++)
                data.setData(pos++, i);

        StringWriter w = new StringWriter();
        data.saveTo(w);

        assertEquals("v2.0 raw\n" +
                "0\n" +
                "1\n" +
                "1\n" +
                "2\n" +
                "2\n" +
                "2\n" +
                "4*3\n" +
                "5*4\n" +
                "6*5\n" +
                "7*6\n" +
                "8*7\n" +
                "9*8\n" +
                "10*9\n", w.toString().replace("\r",""));

        DataField readData = new DataField(100);
        LogisimReader r = new LogisimReader(new StringReader(w.toString()));
        r.read(new ValueArray() {
            @Override
            public void set(int index, long value) {
                readData.setData(index, value);
            }

            @Override
            public long get(int index) {
                return 0;
            }

            @Override
            public int getBytesPerValue() {
                return 0;
            }
        });
    }

    public void testSaveRLE2() throws IOException {
        DataField data = new DataField(100);
        int pos = 0;
        for (int i = 0; i < 10; i++)
            for (int j = 0; j <= i; j++)
                data.setData(pos++, i);

        StringWriter w = new StringWriter();
        data.saveTo(w);

        DataField readData = new DataField(100);
        LogisimReader r = new LogisimReader(new StringReader(w.toString()));
        r.read(new ValueArray() {
            @Override
            public void set(int index, long value) {
                readData.setData(index, value);
            }

            @Override
            public long get(int index) {
                return 0;
            }

            @Override
            public int getBytesPerValue() {
                return 0;
            }
        });
        readData.trim();

        assertTrue(Arrays.equals(data.getData(), readData.getData()));
    }

}
