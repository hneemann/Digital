package de.neemann.digital.gui.components.modification;

import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.graphics.Vector;

/**
 * Created by hneemann on 26.05.17.
 */
public class ModifyMoveAndRotElement extends ModificationOfVisualElement {
    private final Vector pos;
    private final int rotation;

    public ModifyMoveAndRotElement(VisualElement ve, Vector initialPos) {
        super(ve, initialPos);
        pos=ve.getPos();
        rotation = ve.getRotate();
    }

    @Override
    public void modify(Circuit circuit) {
        VisualElement ve = getVisualElement(circuit);
        ve.setPos(pos);
        ve.setRotation(rotation);
    }
}
