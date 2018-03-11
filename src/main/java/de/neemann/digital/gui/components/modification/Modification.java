/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.modification;

import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.library.ElementLibrary;

/**
 * Interface to implement the events used to be reverted
 */
public interface Modification {

    /**
     * Performs a modification on the given circuit
     *
     * @param circuit the circuit to modify
     * @param library the library
     */
    void modify(Circuit circuit, ElementLibrary library);

}
