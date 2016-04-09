package de.neemann.digital.core.element;

/**
 * Interface is used to implement a factory for elements
 *
 * @author hneemann
 */
public interface ElementFactory {
    /**
     * creates a new element matching the given attributes
     *
     * @param attributes the attributes describing the element
     * @return the created element
     */
    Element create(ElementAttributes attributes);
}
