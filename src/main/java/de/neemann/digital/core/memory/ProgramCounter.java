/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.memory;

/**
 * This interface is implemented by all components which are able to act as a program counter.
 */
public interface ProgramCounter {

    /**
     * @return true if this component is used as the program counter
     */
    boolean isProgramCounter();

    /**
     * @return the value of the program counter
     */
    long getProgramCounter();

}
