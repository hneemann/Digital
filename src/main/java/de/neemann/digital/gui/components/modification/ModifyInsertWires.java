/*
 * Copyright (c) 2020 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.modification;

import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.Wire;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.lang.Lang;
import de.neemann.digital.undo.Modification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Modifier to insert a list of wires.
 */
public final class ModifyInsertWires implements Modification<Circuit> {

    /**
     * Creates a simplified modification.
     *
     * @param newWires the wires to insert
     * @return the modification
     */
    public static Modification<Circuit> create(List<Wire> newWires) {
        switch (newWires.size()) {
            case 0:
                return null;
            case 1:
                return new ModifyInsertWire(newWires.get(0));
            default:
                return new ModifyInsertWires(newWires);
        }
    }

    private final ArrayList<LocalWire> wires;

    /**
     * Creates a new instance
     *
     * @param newWires the wire to insert
     */
    private ModifyInsertWires(Collection<Wire> newWires) {
        wires = new ArrayList<>(newWires.size());
        for (Wire w : newWires)
            wires.add(new LocalWire(w));
    }

    @Override
    public void modify(Circuit circuit) {
        ArrayList<Wire> wl = new ArrayList<>(wires.size());
        for (LocalWire w : wires)
            wl.add(new Wire(w.p1, w.p2));
        circuit.add(wl);
    }

    @Override
    public String toString() {
        return Lang.get("mod_insertedWire");
    }

    private static final class LocalWire {
        private final Vector p1;
        private final Vector p2;

        private LocalWire(Wire w) {
            this.p1 = w.p1;
            this.p2 = w.p2;
        }
    }
}
