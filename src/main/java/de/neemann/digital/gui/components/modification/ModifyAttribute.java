package de.neemann.digital.gui.components.modification;

import de.neemann.digital.core.element.Key;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.VisualElement;

/**
 * Created by hneemann on 25.05.17.
 */
public class ModifyAttribute<VALUE> extends ModificationOfVisualElement {

    private final Key<VALUE> key;
    private final VALUE value;

    public ModifyAttribute(VisualElement ve, Key<VALUE> key, VALUE value) {
        super(ve);
        this.key = key;
        this.value = value;
    }

    @Override
    public void modify(Circuit circuit) {
        VisualElement ve = getVisualElement(circuit);
        ve.getElementAttributes().set(key, value);
    }
}
