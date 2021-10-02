/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.memory.importer;

import de.neemann.digital.core.memory.DataField;
import de.neemann.digital.integration.Resources;
import junit.framework.TestCase;

import java.io.*;

public class IntegrationTest extends TestCase {

    public void testSimple() throws MultiValueArray.ValueArrayException, IOException {
        DataField df1 = new DataField(1024);
        DataField df2 = new DataField(1024);

        InputStream data = new ByteArrayInputStream("Hello World!".getBytes());

        new BinReader(data)
                .read(new ByteArrayFromValueArray(
                        new MultiValueArray.Builder()
                                .add(df1, 16)
                                .add(df2, 16)
                                .build()));

        check(df1, 0, "He");
        check(df2, 0, "ll");
        check(df1, 1, "o ");
        check(df2, 1, "Wo");
        check(df1, 2, "rl");
        check(df2, 2, "d!");
    }

    private void check(DataField dataField, int addr, String str) {
        long value = (byte) str.charAt(0) | (((byte) str.charAt(1)) << 8);
        assertEquals(value, dataField.getDataWord(addr));
    }

    public void testLittleEndianHex16() throws IOException {
        File f = new File(Resources.getRoot(), "endianness/test.hex");
        DataField data = Importer.read(f, 16, false);
        assertEquals(0x940c, data.getDataWord(0));
        assertEquals(0x38, data.getDataWord(1));
        assertEquals(0x940c, data.getDataWord(2));
    }

    public void testBigEndianHex16() throws IOException {
        File f = new File(Resources.getRoot(), "endianness/test.hex");
        DataField data = Importer.read(f, 16, true);
        assertEquals(0x0c94, data.getDataWord(0));
        assertEquals(0x3800, data.getDataWord(1));
        assertEquals(0x0c94, data.getDataWord(2));
    }

    public void testLittleEndianHex32() throws IOException {
        File f = new File(Resources.getRoot(), "endianness/test.hex");
        DataField data = Importer.read(f, 32, false);
        assertEquals(0x38940c, data.getDataWord(0));
        assertEquals(0x42940c, data.getDataWord(1));
        assertEquals(0x42940c, data.getDataWord(2));
    }

    public void testBigEndianHex32() throws IOException {
        File f = new File(Resources.getRoot(), "endianness/test.hex");
        DataField data = Importer.read(f, 32, true);
        assertEquals(0x0c943800, data.getDataWord(0));
        assertEquals(0x0c944200, data.getDataWord(1));
        assertEquals(0x0c944200, data.getDataWord(2));
    }

}
