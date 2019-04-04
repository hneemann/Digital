/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.quinemc;

/**
 * A simple bool table
 */
public interface BoolTable {
    /**
     * @return the table row count
     */
    int size();

    /**
     * returns the value at the given row
     *
     * @param i the index
     * @return the value
     */
    ThreeStateValue get(int i);

    /**
     * @return the real size of the bool table
     */
    default int realSize() {
        return size();
    }
}
