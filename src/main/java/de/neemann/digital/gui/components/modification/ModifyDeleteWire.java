/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.modification;

import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.Wire;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.lang.Lang;

/**
 * Modifier to delete a wire
 */
public class ModifyDeleteWire extends ModificationOfWire {

    /**
     * Creates a new instance
     *
     * @param wire       the wire to delete
     * @param initialPos its initial position
     */
    public ModifyDeleteWire(Wire wire, Vector initialPos) {
        super(wire, initialPos, Lang.get("mod_wireDeleted"));
    }

    @Override
    public void modify(Circuit circuit, ElementLibrary library) {
        circuit.delete(getWire(circuit));
    }
}
