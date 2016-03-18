package de.neemann.digital.core.part;

import de.neemann.digital.gui.draw.shapes.Shape;

/**
 * @author hneemann
 */
public interface ShapeFactory {
    Shape create(PartAttributes partAttributes);
}
