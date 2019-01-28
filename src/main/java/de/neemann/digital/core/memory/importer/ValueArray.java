/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.memory.importer;

/**
 * Abstraction of a multi byte value array.
 */
public interface ValueArray {
    /**
     * Sets a value to the given index
     *
     * @param index the indesx
     * @param value the value to set
     */
    void set(int index, long value);

    /**
     * Returns a value from the array
     *
     * @param index the index
     * @return the value
     */
    long get(int index);

    /**
     * @return the number of bytes used in the values
     */
    int getBytesPerValue();
}
