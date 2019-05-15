/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.modification;

import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.lang.Lang;
import de.neemann.digital.undo.Modification;

import static de.neemann.digital.gui.components.modification.ModificationOfVisualElement.getToolTipName;

/**
 * Modifier to insert an element
 */
public class ModifyInsertElement implements Modification<Circuit> {
    private final VisualElement element;

    /**
     * Creates a new instance
     *
     * @param element the element to insert
     */
    public ModifyInsertElement(VisualElement element) {
        this.element = new VisualElement(element);
    }

    @Override
    public void modify(Circuit circuit) {
        circuit.add(new VisualElement(element));
    }

    @Override
    public String toString() {
        return Lang.get("mod_insertedElement_N", getToolTipName(element));
    }
}
