package de.neemann.digital.gui.draw.library;

import de.neemann.digital.core.element.ElementTypeDescription;

/**
 * @author hneemann
 */
public interface ElementNotFoundNotification {
    ElementTypeDescription notFound(String elementName);
}
