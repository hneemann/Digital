/*
 * Copyright (c) 2026
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.modification;

import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.CircuitSelection;
import de.neemann.digital.lang.Lang;
import de.neemann.digital.undo.Modification;

/** Deletes a selection in one undoable edit. */
public final class ModifyDeleteSelection implements Modification<Circuit> {
    private final CircuitSelection selection;

    /**
     * Keeps a snapshot for undo and redo.
     * @param selection the selected objects before deletion
     */
    public ModifyDeleteSelection(CircuitSelection selection) {
        this.selection = new CircuitSelection(selection);
    }

    @Override
    public void modify(Circuit circuit) {
        selection.deleteFrom(circuit);
    }

    @Override
    public String toString() {
        return Lang.get("mod_deletedSelection");
    }
}
