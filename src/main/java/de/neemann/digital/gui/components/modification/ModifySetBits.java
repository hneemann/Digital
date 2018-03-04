/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.modification;

import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.Movable;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.lang.Lang;

import java.util.ArrayList;

/**
 * Sets the bits of the selected elements
 */
public class ModifySetBits implements Modification {

    private final Vector min;
    private final Vector max;
    private final int bits;

    /**
     * Creates a new instance
     *
     * @param c1   first corner of selection
     * @param c2   second corner of selection
     * @param bits the bits to set
     */
    public ModifySetBits(Vector c1, Vector c2, int bits) {
        this.bits = bits;
        min = Vector.min(c1, c2);
        max = Vector.max(c1, c2);
    }

    @Override
    public void modify(Circuit circuit, ElementLibrary library) {
        ArrayList<Movable> list = circuit.getElementsToMove(min, max);
        for (Movable m : list)
            if (m instanceof VisualElement) {
                VisualElement ve = (VisualElement) m;
                try {
                    ElementTypeDescription td = library.getElementType(ve.getElementName());
                    if (td != null) {
                        if (td.getAttributeList().contains(Keys.BITS))
                            ve.setAttribute(Keys.BITS, bits);
                    }
                } catch (ElementNotFoundException e) {
                    e.printStackTrace();
                }
            }

        circuit.modified();
    }

    /**
     * Checks if there are relevant elements in the rectangle
     *
     * @param circuit the circuit
     * @param library the library
     * @return true if there are components with a bits attribute
     */
    public boolean isSomethingToDo(Circuit circuit, ElementLibrary library) {
        ArrayList<Movable> list = circuit.getElementsToMove(min, max);
        for (Movable m : list)
            if (m instanceof VisualElement) {
                VisualElement ve = (VisualElement) m;
                try {
                    ElementTypeDescription td = library.getElementType(ve.getElementName());
                    if (td != null) {
                        if (td.getAttributeList().contains(Keys.BITS))
                            if (ve.getElementAttributes().get(Keys.BITS) != bits)
                                return true;
                    }
                } catch (ElementNotFoundException e) {
                    e.printStackTrace();
                }
            }
        return false;
    }

    @Override
    public String toString() {
        return Lang.get("mod_set_N_BitsToSelection", bits);
    }
}
