/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.modification;

import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.Wire;
import de.neemann.digital.draw.graphics.Vector;

/**
 * A modification on a wire
 * The wire is identified by its position.
 */
public abstract class ModificationOfWire implements Modification {

    private final Vector p1;
    private final String description;
    private final Vector p2;


    /**
     * Creates a new instance
     *
     * @param wire        the wire to modify
     * @param description description of modification
     */
    public ModificationOfWire(Wire wire, String description) {
        this(wire, wire.p1, description);
    }

    /**
     * Creates a new instance
     *
     * @param wire        the wire to modify
     * @param initialPos  the initial position of the wire
     * @param description description of modification
     */
    public ModificationOfWire(Wire wire, Vector initialPos, String description) {
        this.description = description;
        p1 = initialPos;
        p2 = initialPos.add(wire.p2.sub(wire.p1));
    }

    @Override
    public String toString() {
        return description;
    }

    /**
     * Returns the wire to modify
     *
     * @param circuit the circuit to modify
     * @return the wire to modify
     */
    public Wire getWire(Circuit circuit) {
        for (Wire w : circuit.getWires()) {
            if (w.p1.equals(p1) && w.p2.equals(p2))
                return w;
        }
        throw new RuntimeException("internal error: Wire not found!");
    }
}
