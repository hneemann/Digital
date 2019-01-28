/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.memory.importer;

import junit.framework.TestCase;

import java.io.IOException;
import java.io.StringReader;

public class IntelHexReaderTest extends TestCase {

    public void testReadHex() throws IOException {
        String data = ":100000000C9438000C9442000C9442000C94420072\n" +
                ":100010000C9442000C9442000C9442000C94420058\n" +
                ":100020000C9442000C9442000C9442000C94420048\n" +
                ":100030000C9442000C9442000C9442000C94420038\n" +
                ":100040000C9442000C9442000C9442000C94420028\n" +
                ":100050000C9442000C9442000C9442000C94420018\n" +
                ":100060000C9442000C9442000C9442000C94420008\n" +
                ":1000700011241FBECFEFD0E1DEBFCDBF0E944400F0\n" +
                ":100080000C944A000C9400008FEF84B986B18095DF\n" +
                ":0800900085B9FCCFF894FFCF05\n" +
                ":00000001FF";

        int[] bin = new int[200];
        new IntelHexReader(null).read(new StringReader(data), (addr, aByte) -> bin[addr] = aByte);

        assertEquals(0x0c, bin[0x00]);
        assertEquals(0x11, bin[0x70]);
        assertEquals(0x24, bin[0x71]);
        assertEquals(0x00, bin[0x7F]);
        assertEquals(0x95, bin[0x8F]);
        assertEquals(0xCF, bin[0x97]);
    }

    public void testReadSeg() throws IOException {
        String data = ":020000020110EB\n" +
                ":0800000085B9FCCFF894FFCF95\n";

        int[] bin = new int[0x11010];
        new IntelHexReader(null).read(new StringReader(data), (addr, aByte) -> bin[addr] = aByte);

        assertEquals(0x00, bin[0x00]);
        assertEquals(0x85, bin[0x1100]);
        assertEquals(0xb9, bin[0x1101]);
    }
}