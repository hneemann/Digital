package de.neemann.digital.gui.components.data;

import de.neemann.digital.draw.elements.ElementOrder;
import de.neemann.digital.draw.elements.VisualElement;

/**
 * @author hneemann
 */
public class MeasurementFilter implements ElementOrder.ElementMatcher {
    @Override
    public boolean matches(VisualElement element) {
        String name = element.getElementName();
        return name.equals("In") || name.equals("Out") || name.equals("LED") || name.equals("Clock") || name.equals("Probe");
    }
}
