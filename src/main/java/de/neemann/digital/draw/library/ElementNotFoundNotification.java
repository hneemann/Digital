package de.neemann.digital.draw.library;

import de.neemann.digital.core.element.ElementTypeDescription;

/**
 * @author hneemann
 */
public interface ElementNotFoundNotification {
    ElementTypeDescription notFound(String elementName);
}
