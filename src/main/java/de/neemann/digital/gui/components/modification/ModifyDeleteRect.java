/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.modification;

import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.lang.Lang;
import de.neemann.digital.undo.Modification;

/**
 * Modifier to delete all elements in a given rectangle
 */
public class ModifyDeleteRect implements Modification<Circuit> {
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
    public void modify(Circuit circuit) {
        circuit.delete(min, max);
    }

    @Override
    public String toString() {
        return Lang.get("mod_deletedSelection");
    }
}
