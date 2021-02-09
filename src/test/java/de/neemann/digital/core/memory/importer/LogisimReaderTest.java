/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.memory.importer;

import de.neemann.digital.core.memory.DataField;
import junit.framework.TestCase;

import java.io.StringReader;

public class LogisimReaderTest extends TestCase {
    public void testLoad() throws Exception {
        String data = "v2.0 raw\n0\n10\nAA\nFF";

        DataField df = new DataField(1024);
        new LogisimReader(new StringReader(data)).read(new DataFieldValueArray(df,0));

        assertEquals(4, df.trim());
        assertEquals(0x00, df.getDataWord(0));
        assertEquals(0x10, df.getDataWord(1));
        assertEquals(0xAA, df.getDataWord(2));
        assertEquals(0xFF, df.getDataWord(3));
    }

    public void testLoadEmpty() throws Exception {
        String data = "v2.0 raw\n";

        DataField df = new DataField(1024);
        new LogisimReader(new StringReader(data)).read(new DataFieldValueArray(df,0));

        assertEquals(0, df.trim());
    }

    public void testLoad64Bit() throws Exception {
        String data = "v2.0 raw\n8000000000000000\n10\nAA\nFF";

        DataField df = new DataField(1024);
        new LogisimReader(new StringReader(data)).read(new DataFieldValueArray(df,0));

        assertEquals(4, df.trim());
        assertEquals(0x8000000000000000L, df.getDataWord(0));
        assertEquals(0x10, df.getDataWord(1));
        assertEquals(0xAA, df.getDataWord(2));
        assertEquals(0xFF, df.getDataWord(3));
    }

    public void testLoadComments() throws Exception {
        String data = "v2.0 raw\n#test1 \n 0 \n#test1\n  #  test2\n10  # test3\n\n\nAA\nFF #test";

        DataField df = new DataField(1024);
        new LogisimReader(new StringReader(data)).read(new DataFieldValueArray(df,0));

        assertEquals(4, df.trim());
        assertEquals(0x00, df.getDataWord(0));
        assertEquals(0x10, df.getDataWord(1));
        assertEquals(0xAA, df.getDataWord(2));
        assertEquals(0xFF, df.getDataWord(3));
    }

    public void testLoadCommentsRealHex() throws Exception {
        String data = "v2.0 raw\n#test1 \n 0x0 \n#test1\n  #  test2\n0x10  # test3\n\n\n0xAA\n0XFF #test";

        DataField df = new DataField(1024);
        new LogisimReader(new StringReader(data)).read(new DataFieldValueArray(df,0));

        assertEquals(4, df.trim());
        assertEquals(0x00, df.getDataWord(0));
        assertEquals(0x10, df.getDataWord(1));
        assertEquals(0xAA, df.getDataWord(2));
        assertEquals(0xFF, df.getDataWord(3));
    }

    public void testLoadRLE() throws Exception {
        String data = "v2.0 raw\n#test1 \n 5*0x0 \n#test1\n  #  test2\n10*0x10  # test3\n\n\n0xAA\n0XFF #test";

        DataField df = new DataField(1024);
        new LogisimReader(new StringReader(data)).read(new DataFieldValueArray(df,0));

        assertEquals(17, df.trim());
        assertEquals(0x00, df.getDataWord(0));
        assertEquals(0x00, df.getDataWord(1));
        assertEquals(0x00, df.getDataWord(2));
        assertEquals(0x00, df.getDataWord(3));
        assertEquals(0x00, df.getDataWord(4));
        assertEquals(0x10, df.getDataWord(5));
        assertEquals(0x10, df.getDataWord(6));
        assertEquals(0x10, df.getDataWord(7));
        assertEquals(0x10, df.getDataWord(8));
        assertEquals(0x10, df.getDataWord(9));
        assertEquals(0x10, df.getDataWord(10));
        assertEquals(0x10, df.getDataWord(11));
        assertEquals(0x10, df.getDataWord(12));
        assertEquals(0x10, df.getDataWord(13));
        assertEquals(0x10, df.getDataWord(14));
        assertEquals(0xAA, df.getDataWord(15));
        assertEquals(0xFF, df.getDataWord(16));
    }

    public void testLoadRLEMultiline() throws Exception {
        String data = "v2.0 raw\n#test1 \n 5*0x0 10 * 0x10 0xAA 0XFF #test";

        DataField df = new DataField(1024);
        new LogisimReader(new StringReader(data)).read(new DataFieldValueArray(df, 0));

        assertEquals(17, df.trim());
        assertEquals(0x00, df.getDataWord(0));
        assertEquals(0x00, df.getDataWord(1));
        assertEquals(0x00, df.getDataWord(2));
        assertEquals(0x00, df.getDataWord(3));
        assertEquals(0x00, df.getDataWord(4));
        assertEquals(0x10, df.getDataWord(5));
        assertEquals(0x10, df.getDataWord(6));
        assertEquals(0x10, df.getDataWord(7));
        assertEquals(0x10, df.getDataWord(8));
        assertEquals(0x10, df.getDataWord(9));
        assertEquals(0x10, df.getDataWord(10));
        assertEquals(0x10, df.getDataWord(11));
        assertEquals(0x10, df.getDataWord(12));
        assertEquals(0x10, df.getDataWord(13));
        assertEquals(0x10, df.getDataWord(14));
        assertEquals(0xAA, df.getDataWord(15));
        assertEquals(0xFF, df.getDataWord(16));
    }


}