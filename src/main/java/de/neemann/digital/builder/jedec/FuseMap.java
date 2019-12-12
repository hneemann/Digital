/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.builder.jedec;

/**
 * Represents the fuses in the fuse map.
 * Fuse data is represented by bytes containing 8 fuses each
 */
public class FuseMap {

    private final int fuses;
    private final byte[] fuseData;

    /**
     * Creates a new instance
     *
     * @param fuses number of fuses
     */
    public FuseMap(int fuses) {
        this.fuses = fuses;
        fuseData = new byte[(fuses - 1) / 8 + 1];
    }

    /**
     * @return the number of fuses
     */
    public int getFuses() {
        return fuses;
    }

    /**
     * Sets the given fuse to one
     *
     * @param fuse the fuse
     */
    public void setFuse(int fuse) {
        setFuse(fuse, true);
    }

    /**
     * Sets the given fuse
     *
     * @param fuse the fuse
     * @param set  true means programmed to 1, false means programmed to 0
     */
    public void setFuse(int fuse, boolean set) {
        int index = fuse >> 3;
        int n = fuse & 7;
        int mask = 1 << n;
        if (set)
            fuseData[index] |= mask;
        else
            fuseData[index] &= ~mask;
    }

    /**
     * Reads the given fuse
     *
     * @param fuse the fuse
     * @return true is fuse is one
     */
    public boolean getFuse(int fuse) {
        int index = fuse >> 3;
        int n = fuse & 7;
        int mask = 1 << n;
        return (fuseData[index] & mask) != 0;
    }

    /**
     * @return the JEDEC checksum
     */
    public int getJedecChecksum() {
        int c = 0;
        for (int i = 0; i < fuseData.length; i++)
            c += fuseData[i] & 0xff;
        return c & 0xffff;
    }

    byte[] getFuseData() {
        return fuseData;
    }
}
