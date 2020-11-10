/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components;

import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.undo.Modification;

/**
 * Interface to modify a cirtuit
 */
public interface CircuitModifier {
    /**
     * Needs to be called on order to modify a circuit
     *
     * @param modification the modification
     */
    void modify(Modification<Circuit> modification);
}
