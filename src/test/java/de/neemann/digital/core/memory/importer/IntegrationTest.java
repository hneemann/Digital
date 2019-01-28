/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.memory.importer;

import de.neemann.digital.core.memory.DataField;
import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

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
}
