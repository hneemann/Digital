package de.neemann.digital.gui.components.modification;

import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.graphics.Vector;

/**
 * A modification on a visual element.
 * The element is identified by its position and name.
 * Created by hneemann on 25.05.17.
 */
public abstract class ModificationOfVisualElement implements Modification {

    private final Vector pos;
    private final String name;

    /**
     * Creates a new instance
     *
     * @param ve the element to modify
     */
    public ModificationOfVisualElement(VisualElement ve) {
        this(ve, ve.getPos());
    }

    /**
     * Creates a new instance
     *
     * @param ve         the element to modify
     * @param initialPos the initial position of the element
     */
    public ModificationOfVisualElement(VisualElement ve, Vector initialPos) {
        name = ve.getElementName();
        pos = initialPos;
    }

    /**
     * Returns the element which is to modify
     *
     * @param circuit the circuit
     * @return the element to modify
     */
    public VisualElement getVisualElement(Circuit circuit) {
        for (VisualElement ve : circuit.getElements()) {
            if (ve.getPos().equals(pos) && ve.getElementName().equals(name))
                return ve;
        }
        throw new RuntimeException("internal error: Element not found!");
    }
}
