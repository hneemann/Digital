/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.memory;

/**
 * Interface to get access to the rams data.
 */
public interface RAMInterface extends ProgramMemory {
    /**
     * @return the {@link DataField} containing the RAMs data
     */
    DataField getMemory();

    /**
     * @return the name of the memory
     */
    String getLabel();

    /**
     * @return the rams size
     */
    int getSize();

    /**
     * @return the data bits
     */
    int getDataBits();

    /**
     * @return the addr bits
     */
    int getAddrBits();
}
