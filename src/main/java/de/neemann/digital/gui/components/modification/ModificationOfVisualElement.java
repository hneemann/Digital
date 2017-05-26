package de.neemann.digital.gui.components.modification;

import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.graphics.Vector;

/**
 * Created by hneemann on 25.05.17.
 */
public abstract class ModificationOfVisualElement implements Modification {

    private final Vector pos;
    private final String name;

    public ModificationOfVisualElement(VisualElement ve) {
        pos = ve.getPos();
        name = ve.getElementName();
    }

    public VisualElement getVisualElement(Circuit circuit) {
        for (VisualElement ve : circuit.getElements()) {
            if (ve.getPos().equals(pos) && ve.getElementName().equals(name))
                return ve;
        }
        throw new RuntimeException("internal error: Element not found!");
    }
}
