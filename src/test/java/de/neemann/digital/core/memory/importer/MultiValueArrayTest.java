/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.memory.importer;

import de.neemann.digital.core.memory.DataField;
import junit.framework.TestCase;

public class MultiValueArrayTest extends TestCase {

    public void testError() {
        DataField df1 = new DataField(1024);
        DataField df2 = new DataField(1024);
        try {
            new MultiValueArray.Builder()
                    .add(df1, 8)
                    .add(df2, 16);

            fail();
        } catch (MultiValueArray.ValueArrayException e) {
            assertTrue(true);
        }
    }

    public void testSimple() throws MultiValueArray.ValueArrayException {
        DataField df1 = new DataField(1024);
        DataField df2 = new DataField(1024);
        ValueArray va = new MultiValueArray.Builder()
                .add(df1, 16)
                .add(df2, 16)
                .build();

        for (int a = 0; a < 10; a++)
            va.set(a, a);


        for (int a = 0; a < 10; a++)
            assertEquals(a, va.get(a));

        for (int i = 0; i < 5; i++) {
            assertEquals(i * 2, df1.getDataWord(i));
            assertEquals(i * 2 + 1, df2.getDataWord(i));
        }
    }

}