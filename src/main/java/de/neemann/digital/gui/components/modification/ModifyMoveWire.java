/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.modification;

import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.Wire;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.lang.Lang;
import de.neemann.digital.undo.ModifyException;

/**
 * Modifier to move a wire
 */
public class ModifyMoveWire extends ModificationOfWire {
    private final Vector delta;

    /**
     * Create a new instance
     *
     * @param wire    the wire to modify
     * @param newWire the new wire
     */
    public ModifyMoveWire(Wire wire, Wire newWire) {
        super(wire, Lang.get("mod_movedWire"));
        delta = newWire.getPos().sub(wire.getPos());
    }

    @Override
    public void modify(Circuit circuit) throws ModifyException {
        getWire(circuit).move(delta);
        circuit.elementsMoved();
    }
}
