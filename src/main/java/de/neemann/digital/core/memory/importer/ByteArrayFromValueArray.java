/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.memory.importer;

/**
 * Converts a ValueArray to a ByteArray.
 * Used to import byte oriented formats to value arrays
 */
public class ByteArrayFromValueArray implements ByteArray {

    private final ValueArray valueArray;
    private final int div;
    private final boolean bigEndian;

    /**
     * Creates a new instance
     *
     * @param valueArray the valueArray to write to
     */
    public ByteArrayFromValueArray(ValueArray valueArray) {
        this(valueArray, false);
    }

    /**
     * Creates a new instance
     *
     * @param valueArray the valueArray to write to
     * @param bigEndian  true if big endian needs to be used
     */
    public ByteArrayFromValueArray(ValueArray valueArray, boolean bigEndian) {
        this.valueArray = valueArray;
        this.bigEndian = bigEndian;
        div = valueArray.getBytesPerValue();
    }

    @Override
    public void set(int addr, int aByte) {
        int a = addr / div;
        int b = addr % div;

        if (bigEndian)
            b = div - b - 1;

        long val = valueArray.get(a);
        val = val | ((((long) aByte) & 0xff) << (b * 8));
        valueArray.set(a, val);
    }
}
