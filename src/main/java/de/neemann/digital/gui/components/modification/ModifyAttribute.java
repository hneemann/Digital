/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.modification;

import de.neemann.digital.core.element.Key;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.lang.Lang;
import de.neemann.digital.undo.ModifyException;

/**
 * Modifies an attribute.
 *
 * @param <VALUE> the used value type
 */
public class ModifyAttribute<VALUE> extends ModificationOfVisualElement {

    private final Key<VALUE> key;
    private final VALUE value;

    /**
     * Creates a new instance
     *
     * @param ve    the visual element to modify
     * @param key   the key to modify
     * @param value the new value
     */
    public ModifyAttribute(VisualElement ve, Key<VALUE> key, VALUE value) {
        super(ve, Lang.get("mod_setKey_N0_in_element_N1", key.getName(), getToolTipName(ve)));
        this.key = key;
        this.value = value;
    }

    @Override
    public void modify(Circuit circuit) throws ModifyException {
        VisualElement ve = getVisualElement(circuit);
        ve.getElementAttributes().set(key, value);
    }

    /**
     * @return the modified value, only used in some tests
     */
    public VALUE getValue() {
        return value;
    }
}
