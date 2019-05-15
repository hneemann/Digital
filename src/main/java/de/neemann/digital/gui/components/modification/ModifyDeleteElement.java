/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.modification;

import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.lang.Lang;
import de.neemann.digital.undo.ModifyException;

/**
 * Modifier which deletes an element
 */
public class ModifyDeleteElement extends ModificationOfVisualElement {

    /**
     * The element to delete
     *
     * @param ve         the visual element
     */
    public ModifyDeleteElement(VisualElement ve) {
        super(ve, Lang.get("mod_deletedElement_N", getToolTipName(ve)));
    }

    @Override
    public void modify(Circuit circuit) throws ModifyException {
        circuit.delete(getVisualElement(circuit));
    }
}
