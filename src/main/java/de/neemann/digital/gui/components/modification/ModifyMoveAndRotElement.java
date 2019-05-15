/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.modification;

import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.lang.Lang;
import de.neemann.digital.undo.ModifyException;

/**
 * Modifier to move and rotate a single visual element
 */
public class ModifyMoveAndRotElement extends ModificationOfVisualElement {
    private final Vector pos;
    private final int rotation;

    /**
     * Create a new instance
     *
     * @param ve       the visual Element
     * @param pos      the new position
     * @param rotation the new rotation
     */
    public ModifyMoveAndRotElement(VisualElement ve, Vector pos, int rotation) {
        super(ve, Lang.get("mod_movedOrRotatedElement_N", getToolTipName(ve)));
        this.pos = pos;
        this.rotation = rotation;
    }

    @Override
    public void modify(Circuit circuit) throws ModifyException {
        VisualElement ve = getVisualElement(circuit);
        ve.setPos(pos);
        ve.setRotation(rotation);
    }
}
