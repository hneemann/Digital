package de.neemann.digital.gui.components.modification;

import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.Wire;
import de.neemann.digital.draw.graphics.Vector;

/**
 * Created by hneemann on 26.05.17.
 */
public class ModifyMoveWire extends ModificationOfWire {
    private final Vector delta;

    public ModifyMoveWire(Wire wire, Vector initialPos) {
        super(wire, initialPos);
        delta=wire.getPos().sub(initialPos);
    }

    @Override
    public void modify(Circuit circuit) {
        getWire(circuit).move(delta);
        circuit.elementsMoved();
    }
}
