/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.stats;

/**
 * Used by the {@link Statistics} class. Makes a node countable.
 */
public interface Countable {

    /**
     * @return the number of data  bits used
     */
    int getDataBits();

    /**
     * @return the number of inputs
     */
    default int getInputsCount() {
        return 0;
    }

    /**
     * @return the number of address bits
     */
    default int getAddrBits() {
        return 0;
    }

}
