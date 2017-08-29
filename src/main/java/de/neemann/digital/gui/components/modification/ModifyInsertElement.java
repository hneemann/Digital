package de.neemann.digital.gui.components.modification;

import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.lang.Lang;

import static de.neemann.digital.gui.components.modification.ModificationOfVisualElement.getToolTipName;

/**
 * Modifier to insert an element
 * Created by hneemann on 26.05.17.
 */
public class ModifyInsertElement implements Modification {
    private final VisualElement element;

    /**
     * Creates a new instance
     *
     * @param element the element to insert
     */
    public ModifyInsertElement(VisualElement element) {
        this.element = new VisualElement(element);
    }

    @Override
    public void modify(Circuit circuit, ElementLibrary library) {
        circuit.add(new VisualElement(element));
    }

    @Override
    public String toString() {
        return Lang.get("mod_insertedElement_N", getToolTipName(element));
    }
}
