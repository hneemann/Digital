package de.neemann.digital.gui.components.modification;

import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.Wire;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.lang.Lang;

/**
 * Modifier to move a wire
 * Created by hneemann on 26.05.17.
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
