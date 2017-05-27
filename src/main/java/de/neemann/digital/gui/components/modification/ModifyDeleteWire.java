package de.neemann.digital.gui.components.modification;

import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.Wire;
import de.neemann.digital.draw.graphics.Vector;

/**
 * Modifier to delete a wire
 * Created by hneemann on 26.05.17.
 */
public class ModifyDeleteWire extends ModificationOfWire {

    /**
     * Creates a new instance
     *
     * @param wire       the wire to delete
     * @param initialPos its initial position
     */
    public ModifyDeleteWire(Wire wire, Vector initialPos) {
        super(wire, initialPos);
    }

    @Override
    public void modify(Circuit circuit) {
        circuit.delete(getWire(circuit));
    }
}
