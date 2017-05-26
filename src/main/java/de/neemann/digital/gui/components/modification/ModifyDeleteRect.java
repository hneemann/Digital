package de.neemann.digital.gui.components.modification;

import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.graphics.Vector;

/**
 * Created by hneemann on 26.05.17.
 */
public class ModifyDeleteRect implements Modification {
    private final Vector min;
    private final Vector max;

    public ModifyDeleteRect(Vector min, Vector max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public void modify(Circuit circuit) {
        circuit.delete(min, max);
    }
}
