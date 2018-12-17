/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.memory;

import junit.framework.TestCase;

public class DataFieldImporterTest extends TestCase {

    public void testCreate16() {
        DataField df = new DataField(16);
        DataFieldImporter.DataArray a = DataFieldImporter.create(df, 16);
        a.put(0, 0x01);
        a.put(1, 0x10);
        a.put(2, 0x77);
        a.put(3, 0x33);

        assertEquals(0x1001, df.getDataWord(0));
        assertEquals(0x3377, df.getDataWord(1));
    }

    public void testCreate24() {
        DataField df = new DataField(16);
        DataFieldImporter.DataArray a = DataFieldImporter.create(df, 24);
        a.put(0, 0x01);
        a.put(1, 0x10);
        a.put(2, 0x77);
        a.put(3, 0x33);

        assertEquals(0x771001, df.getDataWord(0));
        assertEquals(0x33, df.getDataWord(1));
    }

    public void testCreate32() {
        DataField df = new DataField(16);
        DataFieldImporter.DataArray a = DataFieldImporter.create(df, 32);
        a.put(0, 0x01);
        a.put(1, 0x10);
        a.put(2, 0x77);
        a.put(3, 0x33);
        a.put(4, 0x11);

        assertEquals(0x33771001, df.getDataWord(0));
        assertEquals(0x11, df.getDataWord(1));
    }
}