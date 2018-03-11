/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.builder.jedec;

import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 */
public class JedecWriterTest extends TestCase {

    // Example from JEDEC Standard JESD3-C,  Chap. 3.2
    public void testChecksum() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JedecWriter w = new JedecWriter(baos);
        w.println("TEST*");
        w.println("QF0384*");
        w.println("F0*  ");
        w.println("L10 101*");
        w.close();
        assertEquals(0x5c4, w.getChecksum());

        assertEquals("\u0002TEST*\r\n" +
                "QF0384*\r\n" +
                "F0*  \r\n" +
                "L10 101*\r\n" +
                "\u000305C4", baos.toString());

    }

    public void testFuseMap() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JedecWriter w = new JedecWriter(baos);
        w.println("Test*");
        w.write(FuseMapTest.createFuseMap());
        w.close();

        assertEquals("\u0002Test*\r\n" +
                "QF500*\r\n" +
                "G0*\r\n" +
                "F0*\r\n" +
                "L0 01001110000010001111000011111111*\r\n" +
                "L32 01010001000000000000000000000000*\r\n" +
                "C021A*\r\n" +
                "\u0003141D", baos.toString());
    }
}
