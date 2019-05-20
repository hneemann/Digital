/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.builder.tt2;

import junit.framework.TestCase;

public class RotationByteArrayOutputStreamTest extends TestCase {

    public void testSimple() {
        RotationByteArrayOutputStream r = new RotationByteArrayOutputStream(10);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 30; i++) {
            r.write(i + 'A');
            sb.append((char) (i + 'A'));

            if (i < 10) {
                assertEquals(0, r.getSkipped());
            } else {
                assertEquals(i - 9, r.getSkipped());
            }

            String expected = sb.toString();
            if (expected.length() > 10)
                expected = expected.substring(expected.length() - 10);
            assertEquals(expected, new String(r.toByteArray()));
        }
        assertEquals("skipped bytes: 20\n" +
                "UVWXYZ[\\]^",r.toString());
    }

}