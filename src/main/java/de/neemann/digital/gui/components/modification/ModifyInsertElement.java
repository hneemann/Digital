package de.neemann.digital.gui.components.modification;

import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.VisualElement;

/**
 * Created by hneemann on 26.05.17.
 */
public class ModifyInsertElement implements Modification {
    private final VisualElement element;

    public ModifyInsertElement(VisualElement element) {
        this.element = new VisualElement(element);
    }

    @Override
    public void modify(Circuit circuit) {
        circuit.add(new VisualElement(element));
    }
}
