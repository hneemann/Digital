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
 * Modifier to move a wire
 */
public class ModifyMoveWire extends ModificationOfWire {
    private final Vector delta;

    /**
     * Create a new instance
     *
     * @param wire       the wire to modify
     * @param initialPos its initial position
     */
    public ModifyMoveWire(Wire wire, Vector initialPos) {
        super(wire, initialPos, Lang.get("mod_movedWire"));
        delta = wire.getPos().sub(initialPos);
    }

    @Override
    public void modify(Circuit circuit, ElementLibrary library) {
        getWire(circuit).move(delta);
        circuit.elementsMoved();
    }
}
