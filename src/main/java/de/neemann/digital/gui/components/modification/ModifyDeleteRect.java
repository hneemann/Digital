package de.neemann.digital.gui.components.modification;

import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.lang.Lang;

/**
 * Modifier to delete all elements in a given rectangle
 * Created by hneemann on 26.05.17.
 */
public class ModifyDeleteRect implements Modification {
    private final Vector min;
    private final Vector max;

    /**
     * Creates a new instance
     *
     * @param min the upper left corner
     * @param max the lower right corner
     */
    public ModifyDeleteRect(Vector min, Vector max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public void modify(Circuit circuit, ElementLibrary library) {
        circuit.delete(min, max);
    }

    @Override
    public String toString() {
        return Lang.get("mod_deletedSelection");
    }
}
