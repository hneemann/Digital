package de.neemann.digital.gui.components.modification;

import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.graphics.Vector;

/**
 * Created by hneemann on 26.05.17.
 */
public class ModifyDeleteElement extends ModificationOfVisualElement {
    public ModifyDeleteElement(VisualElement ve, Vector initialPos) {
        super(ve, initialPos);
    }

    @Override
    public void modify(Circuit circuit) {
        circuit.delete(getVisualElement(circuit));
    }
}
