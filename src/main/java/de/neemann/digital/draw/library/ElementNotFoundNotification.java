package de.neemann.digital.draw.library;

import de.neemann.digital.core.element.ElementTypeDescription;

/**
 * Interface to request unknown {@link ElementTypeDescription}s
 *
 * @author hneemann
 */
public interface ElementNotFoundNotification {
    /**
     * called if the library could not create an element
     *
     * @param elementName the elements name
     * @return the element or null if not loadable
     */
    ElementTypeDescription elementNotFound(String elementName);
}
