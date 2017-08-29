package de.neemann.digital.gui.components.modification;

import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.lang.Lang;

/**
 * Modifier to move and rotate a single visual element
 * Created by hneemann on 26.05.17.
 */
public class ModifyMoveAndRotElement extends ModificationOfVisualElement {
    private final Vector pos;
    private final int rotation;

    /**
     * Create a new instance
     *
     * @param ve         the visual Element
     * @param initialPos its initial position
     */
    public ModifyMoveAndRotElement(VisualElement ve, Vector initialPos) {
        super(ve, initialPos, Lang.get("mod_movedOrRotatedElement_N", getToolTipName(ve)));
        pos = ve.getPos();
        rotation = ve.getRotate();
    }

    @Override
    public void modify(Circuit circuit, ElementLibrary library) {
        VisualElement ve = getVisualElement(circuit);
        ve.setPos(pos);
        ve.setRotation(rotation);
    }
}
