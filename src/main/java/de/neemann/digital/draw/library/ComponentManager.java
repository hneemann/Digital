package de.neemann.digital.draw.library;

import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.draw.shapes.ShapeFactory;

/**
 * Used to add components to the library
 */
public interface ComponentManager {

    /**
     * Called to add a component
     *
     * @param nodePath    the path in the components tree/menu
     * @param description the description of the component
     * @throws InvalidNodeException thrown if a node is chosen which is a component
     */
    void addComponent(String nodePath, ElementTypeDescription description) throws InvalidNodeException;

    /**
     * Called to add a component with a custom shape
     *
     * @param nodePath    the path in the components tree/menu
     * @param description the description of the component
     * @param shape       the shape of the component
     * @throws InvalidNodeException thrown if a node is chosen which is a component
     */
    void addComponent(String nodePath, ElementTypeDescription description, ShapeFactory.Creator shape) throws InvalidNodeException;

}
