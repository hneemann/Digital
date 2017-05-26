package de.neemann.digital.gui.components.modification;

import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.Wire;
import de.neemann.digital.draw.graphics.Vector;

/**
 * Created by hneemann on 25.05.17.
 */
public abstract class ModificationOfWire implements Modification {

    private final Vector p1;
    private final Vector p2;

    public ModificationOfWire(Wire wire) {
        this(wire, wire.p1);
    }

    public ModificationOfWire(Wire wire, Vector initialPos) {
        p1 = initialPos;
        p2 = initialPos.add(wire.p2.sub(wire.p1));
    }

    public Wire getWire(Circuit circuit) {
        for (Wire w : circuit.getWires()) {
            if (w.p1.equals(p1) && w.p2.equals(p2))
                return w;
        }
        throw new RuntimeException("internal error: Element not found!");
    }
}
