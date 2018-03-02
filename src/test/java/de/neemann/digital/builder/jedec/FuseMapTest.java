/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.builder.jedec;

import junit.framework.TestCase;

import java.io.IOException;

/**
 */
public class FuseMapTest extends TestCase {

    public void testInit() throws Exception {
        FuseMap fm = new FuseMap(8);

        assertEquals(1, fm.getFuseData().length);
        assertEquals(0, fm.getFuseData()[0]);
    }

    public void testSet() throws Exception {
        FuseMap fm = new FuseMap(8);
        fm.setFuse(0);

        assertEquals(1, fm.getFuseData().length);
        assertEquals(1, fm.getFuseData()[0] & 0xff);
    }

    public void testChecksum() throws IOException {
        FuseMap fs = createFuseMap();
        assertEquals(0x021a, fs.getJedecChecksum());
    }

    // Example from JEDEC Standard JESD3-C,  Chap. 6.2
    static FuseMap createFuseMap() {
        FuseMap fs = new FuseMap(500);
        fs.setFuse(1);
        fs.setFuse(4);
        fs.setFuse(5);
        fs.setFuse(6);
        fs.setFuse(12);
        fs.setFuse(16);
        fs.setFuse(17);
        fs.setFuse(18);
        fs.setFuse(19);
        for (int i = 0; i < 8; i++)
            fs.setFuse(24 + i);
        fs.setFuse(33);
        fs.setFuse(35);
        fs.setFuse(39);
        return fs;
    }

}
