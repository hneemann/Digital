/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.memory.importer;

/**
 * A simple byte array used to read the byte oriented file formats
 */
public interface ByteArray {

    /**
     * Set a byte
     *
     * @param index the index
     * @param aByte the value to set
     */
    void set(int index, int aByte);

}
