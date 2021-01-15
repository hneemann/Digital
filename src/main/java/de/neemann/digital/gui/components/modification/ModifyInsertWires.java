/*
 * Copyright (c) 2020 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.modification;

import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.Wire;
import de.neemann.digital.lang.Lang;
import de.neemann.digital.undo.Modification;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Modifier to insert a list of wires.
 */
public class ModifyInsertWires implements Modification<Circuit> {
    private final ArrayList<Wire> wires;

    /**
     * Creates a new instance
     *
     * @param newWires the wire to insert
     */
    public ModifyInsertWires(Collection<Wire> newWires) {
        wires = new ArrayList<>();
        for (Wire w : newWires)
            wires.add(new Wire(w.p1, w.p2));
    }

    @Override
    public void modify(Circuit circuit) {
        circuit.add(wires);
    }

    @Override
    public String toString() {
        return Lang.get("mod_insertedWire");
    }

}
