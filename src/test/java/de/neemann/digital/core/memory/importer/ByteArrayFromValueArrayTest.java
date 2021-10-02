/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.memory.importer;

import de.neemann.digital.core.memory.DataField;
import junit.framework.TestCase;

public class ByteArrayFromValueArrayTest extends TestCase {

    public void testCreate16() {
        DataField df = new DataField(16);
        ByteArray a = new ByteArrayFromValueArray(new DataFieldValueArray(df, 16));
        a.set(0, 0x01);
        a.set(1, 0x10);
        a.set(2, 0x77);
        a.set(3, 0x33);

        assertEquals(0x1001, df.getDataWord(0));
        assertEquals(0x3377, df.getDataWord(1));
    }

    public void testCreate16BigEndian() {
        DataField df = new DataField(16);
        ByteArray a = new ByteArrayFromValueArray(new DataFieldValueArray(df, 16), true);
        a.set(0, 0x01);
        a.set(1, 0x10);
        a.set(2, 0x77);
        a.set(3, 0x33);

        assertEquals(0x0110, df.getDataWord(0));
        assertEquals(0x7733, df.getDataWord(1));
    }

    public void testCreate24() {
        DataField df = new DataField(16);
        ByteArray a = new ByteArrayFromValueArray(new DataFieldValueArray(df, 24));
        a.set(0, 0x01);
        a.set(1, 0x10);
        a.set(2, 0x77);
        a.set(3, 0x33);

        assertEquals(0x771001, df.getDataWord(0));
        assertEquals(0x33, df.getDataWord(1));
    }

    public void testCreate24BigEndian() {
        DataField df = new DataField(16);
        ByteArray a = new ByteArrayFromValueArray(new DataFieldValueArray(df, 24), true);
        a.set(0, 0x01);
        a.set(1, 0x10);
        a.set(2, 0x77);
        a.set(3, 0x33);

        assertEquals(0x011077, df.getDataWord(0));
        assertEquals(0x330000, df.getDataWord(1));
    }

    public void testCreate32() {
        DataField df = new DataField(16);
        ByteArray a = new ByteArrayFromValueArray(new DataFieldValueArray(df, 32));
        a.set(0, 0x01);
        a.set(1, 0x10);
        a.set(2, 0x77);
        a.set(3, 0x33);
        a.set(4, 0x11);

        assertEquals(0x33771001, df.getDataWord(0));
        assertEquals(0x11, df.getDataWord(1));
    }

    public void testCreate32BigEndian() {
        DataField df = new DataField(16);
        ByteArray a = new ByteArrayFromValueArray(new DataFieldValueArray(df, 32), true);
        a.set(0, 0x01);
        a.set(1, 0x10);
        a.set(2, 0x77);
        a.set(3, 0x33);
        a.set(4, 0x11);

        assertEquals(0x01107733, df.getDataWord(0));
        assertEquals(0x11000000, df.getDataWord(1));
    }
}