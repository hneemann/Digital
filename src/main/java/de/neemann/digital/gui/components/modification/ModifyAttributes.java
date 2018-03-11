/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.modification;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.lang.Lang;

/**
 * Sets all attributes of an element
 */
public class ModifyAttributes extends ModificationOfVisualElement {

    private final ElementAttributes attributes;

    /**
     * Creates a new instance
     *
     * @param ve       the element to modify
     * @param modified the new attributes
     */
    public ModifyAttributes(VisualElement ve, ElementAttributes modified) {
        super(ve, Lang.get("mod_setAttributesIn_N", getToolTipName(ve)));
        attributes = modified;
    }

    @Override
    public void modify(Circuit circuit, ElementLibrary library) {
        VisualElement ve = getVisualElement(circuit);
        ve.getElementAttributes().getValuesFrom(attributes);
    }
}
