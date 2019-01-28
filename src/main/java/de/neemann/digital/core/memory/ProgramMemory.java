/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.memory;

/**
 * Interface to access program memory addresses
 */
public interface ProgramMemory {

    /**
     * @return true is this is program memory
     */
    boolean isProgramMemory();

    /**
     * Called to initialize the memory
     *
     * @param dataField the data to put preload the memory with
     */
    void setProgramMemory(DataField dataField);

    /**
     * @return the data bits
     */
    int getDataBits();

    /**
     * @return the name of the memory
     */
    String getLabel();
}
