/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.modification;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.lang.Lang;

/**
 * Sets all attributes of a circuit
 */
public class ModifyCircuitAttributes implements Modification {
    private final ElementAttributes attributes;

    /**
     * Creates a new instance
     *
     * @param attributes the attributes to set
     */
    public ModifyCircuitAttributes(ElementAttributes attributes) {
        this.attributes = attributes;
    }

    @Override
    public void modify(Circuit circuit, ElementLibrary library) {
        circuit.getAttributes().getValuesFrom(attributes);
    }

    @Override
    public String toString() {
        return Lang.get("mod_circuitAttrModified");
    }
}
