package de.neemann.digital.gui.components.modification;

import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.Wire;
import de.neemann.digital.draw.graphics.Vector;

/**
 * Created by hneemann on 26.05.17.
 */
public class ModifyInsertWire implements Modification {
    private final Vector p1;
    private final Vector p2;

    public ModifyInsertWire(Wire w) {
        p1 = w.p1;
        p2 = w.p2;
    }

    @Override
    public void modify(Circuit circuit) {
        circuit.add(new Wire(p1, p2));
    }
}
