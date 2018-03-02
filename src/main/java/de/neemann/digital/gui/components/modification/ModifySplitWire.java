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
 * Splits a wire into two wires.
 */
public class ModifySplitWire extends ModificationOfWire {
    private final Vector newPoint;

    /**
     * Creates a new instance
     *
     * @param wire     the wire to modify
     * @param newPoint the new point
     */
    public ModifySplitWire(Wire wire, Vector newPoint) {
        super(wire, Lang.get("mod_splitWire"));
        this.newPoint = newPoint;
    }

    @Override
    public void modify(Circuit circuit, ElementLibrary library) {
        Wire w = getWire(circuit);
        Vector p = w.p2;
        w.setP2(newPoint);
        circuit.add(new Wire(newPoint, p));
    }
}
